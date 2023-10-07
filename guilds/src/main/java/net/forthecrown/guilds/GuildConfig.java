package net.forthecrown.guilds;

import java.time.Duration;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public final class GuildConfig {
  private GuildConfig() {}

  private float weekendModifier = 1.5F;
  private float maxExpMultiplier = 8;

  private Duration roleUpdateInterval = Duration.ofMinutes(10);
  private long guildsChannelCategory = 0L;
  private String webhookAvatarPath = "steven_avatar.png";

  private boolean useWebhooks;
}