package net.forthecrown;

import java.lang.StackWalker.Option;
import net.forthecrown.utils.PluginUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Loggers {
  private Loggers() {}

  public static boolean isDebugEnabled() {
    return getLogger().isDebugEnabled();
  }

  public static Logger getLogger() {
    StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
    Class<?> caller = walker.getCallerClass();

    return getLogger(caller.getSimpleName());
  }

  public static Logger getLogger(String name) {
    return LoggerFactory.getLogger(name);
  }

  public static Logger getPluginLogger() {
    var caller = PluginUtil.getCallingPlugin();
    return caller.getSLF4JLogger();
  }
}