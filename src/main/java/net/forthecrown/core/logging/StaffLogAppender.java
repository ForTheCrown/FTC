package net.forthecrown.core.logging;

import static net.kyori.adventure.text.Component.text;

import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.filter.MarkerFilter;

class StaffLogAppender extends AbstractAppender {
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

    var color = fromLevel(event.getLevel());

    StaffChat.newMessage()
        .setPrefix(Text.format("[{0}] ", color, event.getLevel().name()))
        .setMessage(text(formatted))
        .setSource(event.getLoggerName())
        .setDiscordForwarded(false)
        .send();
  }

  TextColor fromLevel(Level level) {
    if (level == Level.WARN) {
      return NamedTextColor.YELLOW;
    }

    if (level == Level.ERROR) {
      return NamedTextColor.RED;
    }

    return NamedTextColor.GRAY;
  }
}