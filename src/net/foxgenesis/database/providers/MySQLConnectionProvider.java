package net.foxgenesis.database.providers;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import net.foxgenesis.database.AConnectionProvider;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLConnectionProvider extends AConnectionProvider {

	private final HikariDataSource source;

	public MySQLConnectionProvider(Properties properties) throws ConnectException {
		super(properties, "MySQL Connection Provider");

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

		try {
			source = new HikariDataSource(new HikariConfig(properties));
		} catch(Exception e) {
			throw new ConnectException("Failed to connect to database");
		}
	}

	@Override
	protected Connection openConnection() throws SQLException { return source.getConnection(); }
}
