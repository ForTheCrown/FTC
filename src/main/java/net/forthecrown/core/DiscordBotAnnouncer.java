package net.forthecrown.core;

public final class DiscordBotAnnouncer {
  private DiscordBotAnnouncer() {}

  public static final String CHANNEL_NAME = "bot_announcements";

  public static void announce(String format, Object... args) {
    FtcDiscord.findChannel(CHANNEL_NAME).ifPresent(channel -> {
      channel.sendMessageFormat(format, args).submit();
    });
  }
}