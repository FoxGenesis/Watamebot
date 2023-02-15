package net.foxgenesis.database;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.internal.utils.IOUtil;
import net.foxgenesis.config.KVPFile;
import net.foxgenesis.util.ResourceUtils.ModuleResource;

public abstract class AbstractDatabase implements AutoCloseable {
	@Nonnull
	private final HashMap<String, String> statements = new HashMap<>();

	@Nonnull
	private final ModuleResource operationsFile;

	@Nonnull
	private final ModuleResource setupFile;

	@Nonnull
	private final String name;

	@Nonnull
	protected final Logger logger;

	@Nullable
	private AConnectionProvider provider;

	public AbstractDatabase(@Nonnull String name, @Nonnull ModuleResource operationsFile,
			@Nonnull ModuleResource setupFile) {
		this.name = Objects.requireNonNull(name);
		this.operationsFile = Objects.requireNonNull(operationsFile);
		this.setupFile = Objects.requireNonNull(setupFile);

		logger = LoggerFactory.getLogger(name);
	}

	final void setup(@Nonnull AConnectionProvider provider) throws IOException {
		if (this.provider != null)
			throw new UnsupportedOperationException("Database is already setup!");

		Objects.requireNonNull(provider);
		logger.debug("Setting up {} with provider {}", name, provider.getName());

		new KVPFile(operationsFile).forEach((id, raw) -> {
			if (!hasStatementID(id))
				this.registerStatement(id, raw);
			else
				logger.error("Statement id {} is already registered!", id);
		});

		this.provider = provider;

		onReady();
	}

	protected abstract void onReady();

	protected Connection openConnection() throws SQLException {
		if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");

		if (provider != null)
			return provider.openConnection();
		throw new UnsupportedOperationException("Database has not been setup yet!");
	}

	protected <U> CompletableFuture<U> prepareStatementAsync(String id, Function<PreparedStatement, U> func) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		else if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");

		if (provider != null) {
			CompletableFuture<PreparedStatement> future = provider.asyncConnection(conn -> {
				try {
					return conn.prepareStatement(getRawStatement(id));
				} catch (SQLException e) {
					throw new CompletionException(e);
				}
			});

			CompletableFuture<PreparedStatement> copy = future.copy();

			return future.thenApplyAsync(func)
					.whenCompleteAsync((result, error) -> { copy.thenAccept(IOUtil::silentClose); });
		}
		return CompletableFuture.failedFuture(new UnsupportedOperationException("Database has not been setup yet!"));
	}

	protected <U> CompletableFuture<U> prepareCallableAsync(String id, Function<CallableStatement, U> func) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		else if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");

		if (provider != null) {
			CompletableFuture<CallableStatement> future = provider.asyncConnection(conn -> {
				try {
					return conn.prepareCall(getRawStatement(id));
				} catch (SQLException e) {
					throw new CompletionException(e);
				}
			});

			CompletableFuture<CallableStatement> copy = future.copy();

			return future.thenApplyAsync(func)
					.whenCompleteAsync((result, error) -> { copy.thenAccept(IOUtil::silentClose); });
		}
		return CompletableFuture.failedFuture(new UnsupportedOperationException("Database has not been setup yet!"));
	}

	protected void prepareStatement(String id, SQLConsumer<PreparedStatement> func, Consumer<Throwable> error) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		else if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");

		if (provider != null) {
			provider.openAutoClosedConnection(conn -> {
				try (PreparedStatement statement = conn.prepareStatement(getRawStatement(id))) {
					func.accept(statement);
					return null;
				}
			}, error);
		} else
			throw new UnsupportedOperationException("Database has not been setup yet!");
	}

	protected void prepareCallable(String id, SQLConsumer<CallableStatement> func, Consumer<Throwable> error) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		else if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");

		if (provider != null) {
			provider.openAutoClosedConnection(conn -> {
				try (CallableStatement statement = conn.prepareCall(getRawStatement(id))) {
					func.accept(statement);
					return null;
				}
			}, error);
		} else
			throw new UnsupportedOperationException("Database has not been setup yet!");
	}

	protected <U> U mapStatement(String id, SQLFunction<PreparedStatement, U> func, Consumer<Throwable> error) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		else if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");

		if (provider != null) {
			return provider.openAutoClosedConnection(conn -> {
				try (PreparedStatement statement = conn.prepareStatement(getRawStatement(id))) {
					return func.apply(statement);
				}
			}, error);
		} else
			throw new UnsupportedOperationException("Database has not been setup yet!");
	}

	protected <U> U mapCallable(String id, SQLFunction<CallableStatement, U> func, Consumer<Throwable> error) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		else if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");

		if (provider != null) {
			return provider.openAutoClosedConnection(conn -> {
				try (CallableStatement statement = conn.prepareCall(getRawStatement(id))) {
					return func.apply(statement);
				}
			}, error);
		} else
			throw new UnsupportedOperationException("Database has not been setup yet!");
	}

	private void registerStatement(@Nonnull String id, @Nonnull String raw) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(raw);

		if (statements.containsKey(id))
			throw new IllegalArgumentException("id [" + id + "] is already registered!");

		logger.debug("Adding statement with id {} [{}]", id, raw);
		statements.put(id, raw);
	}

	protected final boolean hasStatementID(String id) { return statements.containsKey(id); }

	protected final String getRawStatement(String id) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		return statements.get(id);
	}

	final String[] getSetupLines() throws IOException { return setupFile.readAllLines(); }

	@Nonnull
	public final String getName() { return name; }

	public boolean isReady() { return provider != null; }

	@FunctionalInterface
	public interface SQLConsumer<U> { void accept(U u) throws SQLException; }

	@FunctionalInterface
	public interface SQLFunction<U, V> { V apply(U u) throws SQLException; }
}
