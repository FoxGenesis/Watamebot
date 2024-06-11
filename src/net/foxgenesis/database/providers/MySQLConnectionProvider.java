package net.foxgenesis.database.providers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.foxgenesis.database.AConnectionProvider;

public class MySQLConnectionProvider extends AConnectionProvider {

	private final HikariDataSource source;

	public MySQLConnectionProvider(Properties properties) {
		super("MySQL", properties);

		properties.putIfAbsent("dataSource.cachePrepStmts", true);
		properties.putIfAbsent("dataSource.prepStmtCacheSize", 250);
		properties.putIfAbsent("dataSource.prepStmtCacheSqlLimit", 2048);
		properties.putIfAbsent("dataSource.useServerPrepStmts", true);
		properties.putIfAbsent("dataSource.useLocalSessionState", true);
		properties.putIfAbsent("dataSource.rewriteBatchedStatements", true);
		properties.putIfAbsent("dataSource.cacheResultSetMetadata", true);
		properties.putIfAbsent("dataSource.cacheServerConfiguration", true);
		properties.putIfAbsent("dataSource.elideSetAutoCommits", false);
		properties.putIfAbsent("dataSource.maintainTimeStats", true);

		source = new HikariDataSource(new HikariConfig(properties));
	}

	@Override
	protected Connection openConnection() throws SQLException {
		return source.getConnection();
	}

	@Override
	public void close() throws Exception {
		logger.info("Shutting down connection pool");
		source.close();
	}
}
