package net.forthecrown.guilds;

import java.time.Duration;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public final class GuildConfig {
  private GuildConfig() {}

  public float weekendModifier = 1.5F;
  public float maxExpMultiplier = 8;

  public Duration roleUpdateInterval = Duration.ofMinutes(10);

  public long guildsChannelCategory = 0L;

  public String webhookAvatarPath = "steven_avatar.png";

  public boolean weekendMultiplierEnabled = true;
}