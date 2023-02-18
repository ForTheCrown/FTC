package net.forthecrown.core.logging;

import java.lang.StackWalker.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility class for loggers */
public final class Loggers {
  private Loggers() {}

  /** Name of the plugin's LOGGER */
  public static final String PLUGIN_LOGGER_NAME = "FTC";

  public static Logger getLogger() {
    var walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
    var caller = walker.getCallerClass();

    return LogManager.getLogger(caller.getSimpleName());
  }

  public static Logger getPluginLogger() {
    return getLogger(PLUGIN_LOGGER_NAME);
  }

  public static Logger getLogger(String name) {
    return LogManager.getLogger(name);
  }
}