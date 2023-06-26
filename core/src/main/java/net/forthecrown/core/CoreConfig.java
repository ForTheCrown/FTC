package net.forthecrown.core;

import java.time.Duration;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class CoreConfig {

  Duration autosaveInterval = Duration.ofMinutes(30);

  private String[] illegalWorlds = { "world_void", "world_test" };


}
