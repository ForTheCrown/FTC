package net.forthecrown.core;

import java.time.Duration;
import java.time.LocalTime;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ConfigSerializable
public class CoreConfig {

  Duration autosaveInterval = Duration.ofMinutes(30);
  Duration tpCooldown       = Duration.ofSeconds(3);
  Duration tpaExpireTime    = Duration.ofMinutes(3);

  int maxNickLength = 16;

  private String[] illegalWorlds = { "world_void", "world_test" };

  LocalTime dayUpdateTime = LocalTime.of(0, 0, 1);
}
