package net.forthecrown.core.logging;

import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.admin.StaffChat;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.filter.MarkerFilter;

public class StaffLogAppender extends AbstractAppender {
  static final String NAME = StaffLogAppender.class.getSimpleName();
  static final String MARKER_NAME = "staff_log";

  public StaffLogAppender() {
    super(
        NAME,
        MarkerFilter.createFilter(MARKER_NAME, Result.ACCEPT, Result.DENY),
        null,
        true,
        null
    );
    setStarted();
  }

  @Override
  public void append(LogEvent event) {
    var formatted = event.getMessage().getFormattedMessage();
    FtcDiscord.staffLog(event.getLoggerName(), formatted);

    StaffChat.send(Component.text(formatted), false);
  }
}