package net.forthecrown.antigrief;

import java.time.Duration;
import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ConfigSerializable
public class AntiGriefConfig {

  private boolean announcePunishments = false;

  private Map<String, String> defaultReasons;

  private Duration autosaveInterval;

  public @Nullable String getDefaultReason(PunishType type) {
    if (defaultReasons == null) {
      return null;
    }

    return defaultReasons.get(type.name().toLowerCase());
  }
}