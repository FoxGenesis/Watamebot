package net.foxgenesis.watame;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class ProtectedJDABuilder {

	private final JDABuilder builder;

	ProtectedJDABuilder(JDABuilder builder) { this.builder = builder; }

	/**
	 * Adds all provided listeners to the list of listeners that will be used to
	 * populate the {@link net.dv8tion.jda.api.JDA JDA} object. <br>
	 * This uses the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager
	 * InterfacedEventListener} by default. <br>
	 * To switch to the {@link net.dv8tion.jda.api.hooks.AnnotatedEventManager
	 * AnnotatedEventManager}, use
	 * {@link JDA#setEventManager(net.dv8tion.jda.api.hooks.IEventManager)
	 * setEventManager(new AnnotatedEventManager())}.
	 *
	 * <p>
	 * <b>Note:</b> When using the
	 * {@link net.dv8tion.jda.api.hooks.InterfacedEventManager
	 * InterfacedEventListener} (default), given listener(s) <b>must</b> be instance
	 * of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
	 *
	 * @param listeners The listener(s) to add to the list.
	 *
	 * @throws java.lang.IllegalArgumentException If either listeners or one of it's
	 *                                            objects is {@code null}.
	 *
	 * @return The JDABuilder instance. Useful for chaining.
	 *
	 * @see net.dv8tion.jda.api.JDA#addEventListener(Object...)
	 *      JDA.addEventListener(Object...)
	 */
	@Nonnull
	public ProtectedJDABuilder addEventListeners(@Nonnull Object... listeners) {
		builder.addEventListeners(listeners);
		return this;
	}

	/**
	 * Removes all provided listeners from the list of listeners.
	 *
	 * @param listeners The listener(s) to remove from the list.
	 *
	 * @throws java.lang.IllegalArgumentException If either listeners or one of it's
	 *                                            objects is {@code null}.
	 *
	 * @return The JDABuilder instance. Useful for chaining.
	 *
	 * @see net.dv8tion.jda.api.JDA#removeEventListener(Object...)
	 *      JDA.removeEventListener(Object...)
	 */
	@Nonnull
	public ProtectedJDABuilder removeEventListeners(@Nonnull Object... listeners) {
		builder.removeEventListeners(listeners);
		return this;
	}
}