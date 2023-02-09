package net.foxgenesis.database.providers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.foxgenesis.database.AConnectionProvider;

public class MySQLConnectionProvider extends AConnectionProvider {

	private final DataSource source;

	public MySQLConnectionProvider(Properties properties) {
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

		source = new HikariDataSource(new HikariConfig(properties));
	}

	@Override
	protected Connection openConnection() throws SQLException { return source.getConnection(); }
}
