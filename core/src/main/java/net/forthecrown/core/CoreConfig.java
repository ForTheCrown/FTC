package net.forthecrown.core;

import java.time.Duration;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ConfigSerializable
public class CoreConfig {

  Duration autosaveInterval = Duration.ofMinutes(30);
  Duration tpCooldown       = Duration.ofSeconds(3);
  Duration tpaExpireTime    = Duration.ofMinutes(3);

  private String[] illegalWorlds = { "world_void", "world_test" };


}
