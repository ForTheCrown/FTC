package net.forthecrown.core.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;

public class FtcLog {
  public static void addDiscordAppender() {
    var ctx = LoggerContext.getContext(false);
    var config = ctx.getConfiguration();
    var root = config.getRootLogger();

    root.addAppender(new DiscordAppender(), Level.ERROR, null);
    ctx.updateLoggers();
  }
}