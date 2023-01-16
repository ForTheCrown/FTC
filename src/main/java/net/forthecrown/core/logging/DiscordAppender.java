package net.forthecrown.core.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

public class DiscordAppender extends AbstractAppender {

  public DiscordAppender() {
    super("DiscordAppender", null, null, false, null);
    setStarted();
  }

  @Override
  public void append(LogEvent event) {
    if (event.getLevel().intLevel() > Level.ERROR.intLevel()) {
      return;
    }

    DiscordErrorLogHandler.INSTANCE.onLog(
        event.getMessage().getFormattedMessage(),
        event.getThrown(),
        event.getLevel().name(),
        event.getLoggerName()
    );
  }

  static class DiscordErrorHandler implements ErrorHandler {
    @Override
    public void error(String msg) {
      DiscordErrorLogHandler.INSTANCE.onLog(
          msg,
          null,
          "ERROR",
          "Global"
      );
    }

    @Override
    public void error(String msg, Throwable t) {
      DiscordErrorLogHandler.INSTANCE.onLog(
          msg,
          t,
          "ERROR",
          "Global"
      );
    }

    @Override
    public void error(String msg, LogEvent event, Throwable t) {
      DiscordErrorLogHandler.INSTANCE.onLog(
          msg,
          t,
          event.getLevel().name(),
          event.getLoggerName()
      );
    }
  }
}