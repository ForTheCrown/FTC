package net.forthecrown.guilds;

import java.util.concurrent.TimeUnit;
import net.forthecrown.core.config.ConfigData;

@ConfigData(filePath = "ForTheCrown/guilds/config.json")
public final class GuildConfig {
  private GuildConfig() {}

  public static float weekendModifier = 1.5F;
  public static float maxExpMultiplier = 8;

  public static long roleUpdateInterval = TimeUnit.MINUTES.toMillis(10);

  public static long guildsChannelCategory = 0L;

  public static String webhookAvatarPath = "guilds/steven_avatar.png";
}