package net.forthecrown.core.logging;

import static net.forthecrown.core.logging.DiscordAppender.APPENDER_NAME;

import java.lang.StackWalker.Option;
import net.forthecrown.core.config.GeneralConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LoggerContext;

/** Utility class for loggers */
public final class Loggers {
  private Loggers() {}

  /** Name of the plugin's LOGGER */
  public static final String PLUGIN_LOGGER_NAME = "FTC";

  public static final Marker STAFF_LOG
      = MarkerManager.getMarker(StaffLogAppender.NAME);

  public static Logger getLogger() {
    var walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
    var caller = walker.getCallerClass();

    return LogManager.getLogger(caller.getSimpleName());
  }

  public static void updateLoggers() {
    var ctx = LoggerContext.getContext(false);
    var config = ctx.getConfiguration();
    var root = config.getRootLogger();

    root.removeAppender(APPENDER_NAME);
    root.removeAppender(StaffLogAppender.NAME);

    root.addAppender(new DiscordAppender(), getAppenderLevel(), null);
    root.addAppender(new StaffLogAppender(), Level.INFO, null);

    ctx.updateLoggers();
  }

  private static Level getAppenderLevel() {
    String name = GeneralConfig.discordAppenderLevel;
    return Level.toLevel(name, Level.ERROR);
  }

  public static Logger getPluginLogger() {
    return getLogger(PLUGIN_LOGGER_NAME);
  }

  public static Logger getLogger(String name) {
    return LogManager.getLogger(name);
  }
}