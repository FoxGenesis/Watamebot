package net.foxgenesis.watame.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.WatameBot.ProtectedJDABuilder;
import net.foxgenesis.watame.plugin.IPlugin;
import net.foxgenesis.watame.plugin.PluginProperties;

@PluginProperties(name = "Integrated Commands", description = "A plugin that provides default commands", version = "1.0.0", providesCommands = true)
public class IntegratedCommands implements IPlugin {

	@Override
	public void preInit() {}

	@Override
	public void init(ProtectedJDABuilder builder) { builder.addEventListeners(new ConfigCommand(), new PingCommand()); }

	@Override
	public void postInit(WatameBot bot) {}

	@Override
	public void onReady(WatameBot bot) {}

	@Override
	public void close() {}

	@Override
	public Collection<CommandData> getCommands() {
		return List.of(Commands.slash("ping", "Ping the bot to test the connection"),
				Commands.slash("config-get", "Get the configuration of the bot")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
						.setGuildOnly(true)
						.addOption(OptionType.STRING, "key", "Location of the variable", true, false),
				Commands.slash("config-set", "Get the configuration of the bot")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
						.setGuildOnly(true).addOption(OptionType.STRING, "key", "Location of the variable", true, false)
						.addOption(OptionType.STRING, "type", "The variable's type", true, true)
						.addOptions(createAllOptions()));
	}

	private static List<OptionData> createAllOptions() {
		return Arrays.stream(OptionType.values())
				.filter(type -> !(type == OptionType.UNKNOWN || type == OptionType.SUB_COMMAND
						|| type == OptionType.SUB_COMMAND_GROUP))
				.map(type -> new OptionData(type, type.name().toLowerCase(),
						"Value to set of type " + type.name().toLowerCase()).setAutoComplete(false).setRequired(false))
				.toList();
	}
