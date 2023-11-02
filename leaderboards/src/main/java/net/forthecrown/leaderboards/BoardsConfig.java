package net.forthecrown.leaderboards;

import java.time.Duration;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class BoardsConfig {

  private int renderRadius = 25;

  private Duration autosaveInterval = Duration.ofMinutes(30);
}
