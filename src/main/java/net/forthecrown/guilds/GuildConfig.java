package net.forthecrown.guilds;

import net.forthecrown.core.config.ConfigData;

@ConfigData(filePath = "ForTheCrown/guilds/config.json")
public final class GuildConfig {
  private GuildConfig() {}

  public static float weekendModifier = 1.5F;
  public static float maxExpMultiplier = 8;
}