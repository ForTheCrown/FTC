package net.forthecrown.antigrief;

import java.time.Duration;
import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public class AntiGriefConfig {

  private boolean announcePunishments = false;

  @Setting
  private DefaultMessages defaultReasons;

  private Duration autosaveInterval = Duration.ofMinutes(30);

  Material[] veinReporterBlocks = {};

  public @Nullable String getDefaultReason(PunishType type) {
    if (defaultReasons == null) {
      return null;
    }

    return switch (type) {
      case JAIL -> defaultReasons.jail;
      case BAN -> defaultReasons.ban;
      case IP_BAN -> defaultReasons.ip_ban;
      case KICK -> defaultReasons.kick;
      case MUTE -> defaultReasons.mute;
      case SOFT_MUTE -> defaultReasons.soft_mute;
    };
  }

  @ConfigSerializable
  public static class DefaultMessages {
    String mute;
    String soft_mute;
    String ban;
    String ip_ban;
    String kick;
    String jail;
  }
}