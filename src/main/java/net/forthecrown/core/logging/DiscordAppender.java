package net.forthecrown.core.logging;

import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

public class DiscordAppender extends AbstractAppender {
  static final String APPENDER_NAME = DiscordAppender.class.getSimpleName();

  public DiscordAppender() {
    super(APPENDER_NAME, null, null, false, null);
    setStarted();
  }

  @Override
  public void append(LogEvent event) {
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
          ""
      );
    }

    @Override
    public void error(String msg, Throwable t) {
      DiscordErrorLogHandler.INSTANCE.onLog(
          msg,
          t,
          "ERROR",
          ""
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