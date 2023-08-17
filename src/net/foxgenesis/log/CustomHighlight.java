package net.foxgenesis.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class CustomHighlight extends ForegroundCompositeConverterBase<ILoggingEvent> {

	@Override
	protected String getForegroundColorCode(ILoggingEvent event) {
		Level level = event.getLevel();
		return switch (level.toInt()) {
			case Level.ERROR_INT -> "38;5;196";
			case Level.WARN_INT -> "38;5;226";
			case Level.INFO_INT -> "38;5;255";
			case Level.DEBUG_INT -> "36";
			case Level.TRACE_INT -> "38;5;117";
			default -> ANSIConstants.DEFAULT_FG;
		};
	}
}
