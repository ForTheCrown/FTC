package net.forthecrown.core;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class DiscordErrorLogHandler extends Handler {
  static final DiscordErrorLogHandler INSTANCE = new DiscordErrorLogHandler();

  private DiscordErrorLogHandler() {}

  void onLog(String formattedMessage,
             Throwable thrown,
             String levelName,
             String loggerName
  ) {
    // I will never get over that function's name
    TextChannel channel = DiscordSRV.getPlugin()
        .getDestinationTextChannelForGameChannelName("error-log");

    StringBuilder builder = new StringBuilder()
        .append(formattedMessage);

    if (thrown != null) {
      builder.append("\n```\n");
      MessageWriter mWriter = new MessageWriter(builder);
      PrintWriter writer = new PrintWriter(mWriter);
      thrown.printStackTrace(writer);
      builder.append("\n```");
    }

    System.out.print("Log message:\n");
    System.out.print("Length=" + builder.length() + "\n");

    if (channel == null) {
      FtcDiscord.staffLog(levelName, builder.toString());
    } else {
      DiscordUtil.queueMessage(
          channel,
          "**" + levelName + "** [" + loggerName + "]" + builder
      );
    }
  }

  @Override
  public void publish(LogRecord record) {
    if (!shouldLog(record)) {
      return;
    }

    onLog(
        record.getMessage(),
        record.getThrown(),
        record.getLevel().getName(),
        record.getLoggerName()
    );
  }

  private boolean shouldLog(LogRecord record) {
    if (record.getLevel().intValue() < Level.SEVERE.intValue()) {
      return false;
    }

    var thrown = record.getThrown();

    if (thrown == null) {
      return true;
    }

    for (var e: thrown.getStackTrace()) {
      if (e.getClassName().contains("net.forthecrown")) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void flush() {}

  @Override
  public void close() {}

  @Getter
  private static class MessageWriter extends Writer {
    public static final String ERROR_OVER_MAX = "\n... CHAR LIMIT REACHED.";

    private final StringBuilder buffer;
    private final int maxSize;

    public MessageWriter(StringBuilder buffer) {
      this.buffer = buffer;

      // Discord has a 2000 max char limit, so calculate how many
      // characters we can write until we'd pass over that
      maxSize = 2000 - ERROR_OVER_MAX.length() - 15 - buffer.length();
    }

    @Override
    public void write(char @NotNull [] cbuf, int off, int len)
        throws IOException
    {
      if (buffer.length() >= maxSize) {
        return;
      }

      for (int i = 0; i < len; i++) {
        char c = cbuf[i + off];
        buffer.append(c);

        if (buffer.length() >= maxSize) {
          buffer.append(ERROR_OVER_MAX);
          return;
        }
      }
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}
  }
}