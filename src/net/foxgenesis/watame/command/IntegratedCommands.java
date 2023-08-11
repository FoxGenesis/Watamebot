package net.foxgenesis.watame.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;

import org.apache.commons.configuration2.Configuration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class IntegratedCommands extends Plugin {
	@Override
	protected void onConstruct(Properties meta, Map<String, Configuration> configs) {}

	@Override
	public void preInit() {}

	@Override
	public void init(IEventStore builder) {

		builder.registerListeners(this, new ConfigCommand(), new PingCommand());
	}

	@Override
	public void postInit(WatameBot bot) {

	}

	@Override
	public void onReady(WatameBot bot) {}

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
