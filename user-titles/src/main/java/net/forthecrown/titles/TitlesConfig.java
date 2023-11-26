package net.forthecrown.titles;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ConfigSerializable
public class TitlesConfig {

  private int baronPrice;
}
