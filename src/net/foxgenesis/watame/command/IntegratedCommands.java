package net.foxgenesis.watame.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.foxgenesis.util.resource.ConfigType;
import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.require.CommandProvider;
import net.foxgenesis.watame.plugin.require.PluginConfiguration;

import org.apache.commons.configuration2.Configuration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

@PluginConfiguration(defaultFile = "/META-INF/integrated.ini", identifier = "integrated", outputFile = "integrated.ini", type = ConfigType.INI)
public class IntegratedCommands extends Plugin implements CommandProvider {

	private final boolean enableConfigCommand;
	private final boolean enablePingCommand;

	public IntegratedCommands() {
		super();

		if (hasConfiguration("integrated")) {
			Configuration config = getConfiguration("integrated");
			enableConfigCommand = config.getBoolean("IntegratedPlugin.enableOptionsCommand", false);
			enablePingCommand = config.getBoolean("IntegratedPlugin.enablePingCommand", true);
		} else {
			enableConfigCommand = false;
			enablePingCommand = true;
		}
	}

	@Override
	public void preInit() {}

	@Override
	public void init(IEventStore builder) {
		if (enableConfigCommand)
			builder.registerListeners(this, new ConfigCommand());

		if (enablePingCommand)
			builder.registerListeners(this, new PingCommand());
	}

	@Override
	public void postInit() {

	}

	@Override
	public void onReady() {}

	@Override
	public void close() {}

	@Override
	public Collection<CommandData> getCommands() {
		return List.of(Commands.slash("ping", "Ping the bot to test the connection"), getOptionsCommand());

	}

	private static CommandData getOptionsCommand() {
		List<SubcommandGroupData> groups = new ArrayList<>();
		groups.add(getConfigSubCommands());

		return Commands.slash("options", "Change bot options").setGuildOnly(true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
				.addSubcommandGroups(groups);
	}

	private static SubcommandGroupData getConfigSubCommands() {
		List<SubcommandData> commands = new ArrayList<>();

		// Add "set" commands
		for (OptionData option : createAllOptions()) {
			commands.add(new SubcommandData("set-" + option.getName(),
					"Set a " + option.getName() + " value in the configuration")
					.addOption(OptionType.STRING, "key", "Location of the variable", true, true).addOptions(option));
		}

		// Add "get" and "remove"
		commands.add(new SubcommandData("get", "Get a value from the configuration").addOption(OptionType.STRING, "key",
				"Location of the variable", true, true));
		commands.add(new SubcommandData("remove", "Remove a value from the configuration").addOption(OptionType.STRING,
				"key", "Location of the variable", true, true));

		// Add "list"
		commands.add(new SubcommandData("list", "List all configuration values"));
		return new SubcommandGroupData("configuration", "Get/Update plugin configuration").addSubcommands(commands);
	}

	private static List<OptionData> createAllOptions() {
		return Arrays.stream(OptionType.values())
				.filter(type -> !(type == OptionType.UNKNOWN || type == OptionType.SUB_COMMAND
						|| type == OptionType.SUB_COMMAND_GROUP))
				.map(type -> new OptionData(type, type.name().toLowerCase(),
						"Set the value as a " + type.name().toLowerCase()).setAutoComplete(false).setRequired(true))
				.toList();
	}
}
