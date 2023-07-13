package net.forthecrown.discord;

import static github.scarsz.discordsrv.dependencies.jda.api.entities.Message.MAX_CONTENT_LENGTH;
import static net.forthecrown.discord.FtcDiscord.findChannel;

import github.scarsz.discordsrv.util.DiscordUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.utils.VanillaAccess;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.jetbrains.annotations.NotNull;

class DiscordAppender extends AbstractAppender {
  static final String APPENDER_NAME = DiscordAppender.class.getSimpleName();

  static final String BLOCK_PREFIX = "\n```\n";
  static final String BLOCK_SUFFIX = "\n```";

  static final String THROWABLE_TOO_LONG = "\nThrowable too long to include";

  /**
   * List of loggers names that are to be ignored by this appender, as they
   * print meaningless information too frequently.
   * <p>
   * Each entry's reason for being on the list: <pre>
   * 1. Votifier: 'readAddress(..) failed: Connection reset by peer'
   *    Means nothing and is thrown too frequently
   *
   * 2. nms login listener: Logs error messages of what are most likely cracked
   *                        clients trying to join
   *
   * 3. BuycraftX: Cannot process analytic events or something, seems to affect
   *               nothing and just clogs up error channel
   * </pre>
   */
  public static final Set<String> IGNORED_LOGGERS = Set.of(
      "Votifier",
      "net.minecraft.server.network.LoginListener",
      "BuycraftX"
  );

  private final String channelName;

  public DiscordAppender(String channelName) {
    super(APPENDER_NAME, null, null, false, null);
    this.channelName = channelName;

    setStarted();
  }

  @Override
  public void append(LogEvent event) {
    var loggerName = event.getLoggerName();

    // Filter out ignored loggers
    for (var s: IGNORED_LOGGERS) {
      if (loggerName.contains(s)) {
        return;
      }
    }

    var levelName = event.getLevel().name();
    var formattedMessage = event.getMessage().getFormattedMessage();
    var thrown = event.getThrown();

    String prefix = "**" + levelName + "** [" + loggerName + "] ";
    StringBuilder builder = new StringBuilder()
        .append(prefix)
        .append(formattedMessage);

    if (builder.length() >= MAX_CONTENT_LENGTH) {
      builder.delete(MAX_CONTENT_LENGTH - 1, builder.length());
    } else if (thrown != null) {
      int newMinLength = BLOCK_PREFIX.length()
          + BLOCK_SUFFIX.length()
          + MessageWriter.ERROR_OVER_MAX.length()
          + builder.length();

      if (newMinLength < MAX_CONTENT_LENGTH) {
        builder.append(BLOCK_PREFIX);
        MessageWriter writer = new MessageWriter(builder);
        PrintWriter print = new MessagePrint(writer);

        // Print in a different format to conserve characters
        printStackTrace(thrown, print);

        builder.append(BLOCK_SUFFIX);
      } else if ((THROWABLE_TOO_LONG.length() + builder.length()) < MAX_CONTENT_LENGTH) {
        builder.append(THROWABLE_TOO_LONG);
      }
    }

    findChannel(channelName).ifPresent(channel -> {
      DiscordUtil.queueMessage(channel, builder.toString());
    });
  }

  private static void printStackTrace(Throwable t, PrintWriter out) {
    VanillaAccess.deobfuscate(t);
    out.println(t);

    StackTraceElement[] stackTrace = t.getStackTrace();
    for (var trace: stackTrace) {
      String className = trace.getClassName();
      String methodName = trace.getMethodName();
      int lineno = trace.getLineNumber();

      out.println(className + "#" + methodName + ":" + lineno);
    }

    if (t.getCause() != null) {
      printStackTrace(t.getCause(), out);
    }
  }

  private static class MessagePrint extends PrintWriter {
    public MessagePrint(@NotNull Writer out) {
      super(out);
    }

    @Override
    public void println(String x) {
      this.println((Object) x);
    }

    @Override
    public void println(Object x) {
      String s = String.valueOf(x);

      if (s.contains("//")) {
        int index = s.indexOf("//");
        s = s.substring(index + 2);
      } else if (s.startsWith("\tat ")) {
        s = s.substring("\tat ".length());
      }

      if (s.startsWith("net.")
          || s.startsWith("com.")
          || s.startsWith("org.")
      ) {
        s = s.substring(4);
      }

      super.println(s);
    }
  }

  @Getter
  private static class MessageWriter extends Writer {
    public static final String ERROR_OVER_MAX = "\n... CHAR LIMIT REACHED.";

    private final StringBuilder buffer;
    private final int maxSize;

    public MessageWriter(StringBuilder buffer) {
      this.buffer = buffer;

      // Discord has a 2000 max char limit, so calculate how many
      // characters we can write until we'd pass over that
      maxSize = MAX_CONTENT_LENGTH
          - 1
          - ERROR_OVER_MAX.length()
          - BLOCK_SUFFIX.length();
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

        if (c == '\t') {
          continue;
        }

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