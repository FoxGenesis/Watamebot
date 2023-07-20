package net.foxgenesis.database;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.internal.utils.IOUtil;
import net.foxgenesis.config.KVPFile;
import net.foxgenesis.util.resource.ModuleResource;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
public abstract class AbstractDatabase implements AutoCloseable {
	@NotNull
	private final HashMap<String, String> statements = new HashMap<>();

	@NotNull
	private final ModuleResource operationsFile;

	@NotNull
	private final ModuleResource setupFile;

	@NotNull
	private final String name;

	/**
	 * Logger
	 */
	@NotNull
	protected final Logger logger;

	private AConnectionProvider provider;

	/**
	 * NEED_JAVADOC
	 * 
	 * @param name
	 * @param operationsFile
	 * @param setupFile
	 */
	public AbstractDatabase(@NotNull String name, @NotNull ModuleResource operationsFile,
			@NotNull ModuleResource setupFile) {
		this.name = Objects.requireNonNull(name);
		this.operationsFile = Objects.requireNonNull(operationsFile);
		this.setupFile = Objects.requireNonNull(setupFile);

		logger = LoggerFactory.getLogger(name);
	}

	@SuppressWarnings("resource")
	synchronized final void setup(@NotNull AConnectionProvider provider) throws IOException {
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

	synchronized final void unload() {
		IOUtil.silentClose(this);
		provider = null;
	}

	/**
	 * NEED_JAVADOC
	 */
	protected abstract void onReady();

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 * 
	 * @throws SQLException
	 */
	protected Connection openConnection() throws SQLException {
		if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");

		if (provider != null)
			return provider.openConnection();
		throw new UnsupportedOperationException("Database has not been setup yet!");
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param id
	 * @param func
	 * @param error
	 */
	protected void prepareStatement(String id, SQLConsumer<PreparedStatement> func, Consumer<Throwable> error) {
		validate(id);

		provider.openAutoClosedConnection(conn -> {
			try (PreparedStatement statement = conn.prepareStatement(getRawStatement(id))) {
				func.accept(statement);
				return null;
			}
		}, error);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param id
	 * @param func
	 * @param error
	 */
	protected void prepareCallable(String id, SQLConsumer<CallableStatement> func, Consumer<Throwable> error) {
		validate(id);

		provider.openAutoClosedConnection(conn -> {
			try (CallableStatement statement = conn.prepareCall(getRawStatement(id))) {
				func.accept(statement);
				return null;
			}
		}, error);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param <U>
	 * @param id
	 * @param func
	 * @param error
	 * 
	 * @return
	 */
	@NotNull
	protected <U> Optional<U> mapStatement(String id, SQLFunction<PreparedStatement, U> func,
			Consumer<Throwable> error) {
		validate(id);

		return provider.openAutoClosedConnection(conn -> {
			try (PreparedStatement statement = conn.prepareStatement(getRawStatement(id))) {
				return func.apply(statement);
			}
		}, error);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param <U>
	 * @param id
	 * @param func
	 * @param error
	 * 
	 * @return
	 */
	@Nullable
	protected <U> Optional<U> mapCallable(String id, SQLFunction<CallableStatement, U> func,
			Consumer<Throwable> error) {
		validate(id);

		return provider.openAutoClosedConnection(conn -> {
			try (CallableStatement statement = conn.prepareCall(getRawStatement(id))) {
				return func.apply(statement);
			}
		}, error);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param id
	 * 
	 * @return
	 */
	protected final boolean hasStatementID(String id) {
		return statements.containsKey(id);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param id
	 * 
	 * @return
	 */
	protected final String getRawStatement(String id) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		return statements.get(id);
	}

	private void registerStatement(@NotNull String id, @NotNull String raw) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(raw);

		if (statements.containsKey(id))
			throw new IllegalArgumentException("id [" + id + "] is already registered!");

		logger.debug("Adding statement with id {} [{}]", id, raw);
		statements.put(id, raw);
	}

	private void validate(String id) {
		if (!hasStatementID(id))
			throw new NoSuchElementException("No statement exists with id " + id);
		else if (!isReady())
			throw new UnsupportedOperationException("Database has not been setup yet!");
	}

	final String[] getSetupLines() throws IOException {
		return setupFile.readAllLines();
	}

	@NotNull
	public final String getName() {
		return name;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return Returns {@code true} if the database is ready for use
	 */
	public boolean isReady() {
		return provider != null;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @author Ashley
	 *
	 * @param <U>
	 */
	@FunctionalInterface
	public interface SQLConsumer<U> {
		void accept(U u) throws SQLException;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @author Ashley
	 *
	 * @param <U>
	 * @param <V>
	 */
	@FunctionalInterface
	public interface SQLFunction<U, V> {
		V apply(U u) throws SQLException;
	}
}
