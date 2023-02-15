package net.foxgenesis.watame.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.foxgenesis.watame.ProtectedJDABuilder;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.IPlugin;
import net.foxgenesis.watame.plugin.PluginProperties;

@PluginProperties(name = "Integrated Commands", description = "A plugin that provides default commands", version = "1.0.1", providesCommands = true)
public class IntegratedCommands implements IPlugin {
	private final ListenerAdapter[] listeners = { new ConfigCommand(), new PingCommand() };

	@Override
	public void preInit() {}

	@Override
	public void init(ProtectedJDABuilder builder) {
		for (ListenerAdapter listener : listeners)
			builder.addEventListeners(listener);
	}

	@Override
	public void postInit(WatameBot bot) {}

	@Override
	public void onReady(WatameBot bot) {}

	@Override
	public void close() {
		JDA jda = WatameBot.getInstance().getJDA();
		if (jda != null)
			for (ListenerAdapter listener : listeners)
				jda.removeEventListener(listener);
	}

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
						.addOptions(new OptionData(OptionType.STRING, "type", "The variable's type").setRequired(true)
								.setAutoComplete(false).addChoices(ConfigCommand.options))
						.addOptions(createAllOptions()));

	}

	private static List<OptionData> createAllOptions() {
		return Arrays.stream(OptionType.values())
				.filter(type -> !(type == OptionType.UNKNOWN || type == OptionType.SUB_COMMAND
						|| type == OptionType.SUB_COMMAND_GROUP))
				.map(type -> new OptionData(type, type.name().toLowerCase(),
						"Set the value as a " + type.name().toLowerCase()).setAutoComplete(false).setRequired(false))
				.toList();
	}
}
