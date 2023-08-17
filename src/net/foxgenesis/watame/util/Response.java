package net.foxgenesis.watame.util;

import static net.foxgenesis.watame.util.Colors.ERROR;
import static net.foxgenesis.watame.util.Colors.INFO;
import static net.foxgenesis.watame.util.Colors.NOTICE;
import static net.foxgenesis.watame.util.Colors.SUCCESS;
import static net.foxgenesis.watame.util.Colors.WARNING;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public final class Response {

	// =================================================================================================================

	public static MessageEmbed info(@NotNull String description) {
		return builder(INFO, description).build();
	}

	public static MessageEmbed info(@NotNull String title, @NotNull String description) {
		return builder(INFO, title, description).build();
	}

	public static MessageEmbed info(@NotNull String title, @NotNull String url, @NotNull String description) {
		return builder(INFO, title, url, description).build();
	}

	// =================================================================================================================

	public static MessageEmbed success(@NotNull String description) {
		return builder(SUCCESS, description).build();
	}

	public static MessageEmbed success(@NotNull String title, @NotNull String description) {
		return builder(SUCCESS, title, description).build();
	}

	public static MessageEmbed success(@NotNull String title, @NotNull String url, @NotNull String description) {
		return builder(SUCCESS, title, url, description).build();
	}

	// =================================================================================================================

	public static MessageEmbed notice(@NotNull String description) {
		return builder(NOTICE, description).build();
	}

	public static MessageEmbed notice(@NotNull String title, @NotNull String description) {
		return builder(NOTICE, title, description).build();
	}

	public static MessageEmbed notice(@NotNull String title, @NotNull String url, @NotNull String description) {
		return builder(NOTICE, title, url, description).build();
	}

	// =================================================================================================================

	public static MessageEmbed warn(@NotNull String description) {
		return builder(WARNING, description).build();
	}

	public static MessageEmbed warn(@NotNull String title, @NotNull String description) {
		return builder(WARNING, title, description).build();
	}

	public static MessageEmbed warn(@NotNull String title, @NotNull String url, @NotNull String description) {
		return builder(WARNING, title, url, description).build();
	}

	// =================================================================================================================

	public static MessageEmbed error(@NotNull String description) {
		return builder(ERROR, description).build();
	}

	public static MessageEmbed error(@NotNull String title, @NotNull String description) {
		return builder(ERROR, title, description).build();
	}

	public static MessageEmbed error(@NotNull String title, @NotNull String url, @NotNull String description) {
		return builder(ERROR, title, url, description).build();
	}

	// =================================================================================================================

	private static EmbedBuilder builder(int color, @NotNull String description) {
		return new EmbedBuilder().setColor(color).setDescription(description);
	}

	private static EmbedBuilder builder(int color, @NotNull String title, @NotNull String description) {
		return builder(color, description).setTitle(title);
	}

	private static EmbedBuilder builder(int color, @NotNull String title, @NotNull String url,
			@NotNull String description) {
		return builder(color, description).setTitle(title, url);
	}
}
