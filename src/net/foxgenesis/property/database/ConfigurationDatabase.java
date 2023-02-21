package net.foxgenesis.property.database;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.DatabaseConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.DatabaseBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfigurationDatabase {

	private final BasicConfigurationBuilder<DatabaseConfiguration> builder = new BasicConfigurationBuilder<DatabaseConfiguration>(
			DatabaseConfiguration.class);
	
	private final DataSource source;
	private final String table, configNameColumn, keyColumn, valueColumn;
	
	public ConfigurationDatabase(@Nonnull DataSource dataSource, @Nonnull String table,
			@Nonnull String configNameColumn, @Nonnull String keyColumn, @Nonnull String valueColumn) {
		this.source = Objects.requireNonNull(dataSource);
		
		this.table = Objects.requireNonNull(table);
		this.configNameColumn = Objects.requireNonNull(configNameColumn);
		this.keyColumn = Objects.requireNonNull(keyColumn);
		this.valueColumn = Objects.requireNonNull(valueColumn);
	}
	
	public Configuration getConfiguration(@Nonnull String configurationName) throws ConfigurationException {
		builder.resetParameters();
		builder.configure(newConfig(Objects.requireNonNull(configurationName)));
		return builder.getConfiguration();
	}

	private DatabaseBuilderParameters newConfig(String name) {
		return new Parameters().database().setDataSource(source).setTable(table).setKeyColumn(keyColumn)
		.setValueColumn(valueColumn).setConfigurationNameColumn(configNameColumn).setConfigurationName(name);
	}
}
