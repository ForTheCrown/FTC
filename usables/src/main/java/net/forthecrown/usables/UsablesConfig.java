package net.forthecrown.usables;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ConfigSerializable
public class UsablesConfig {

  private String firstJoinKit = "noobs";
}
