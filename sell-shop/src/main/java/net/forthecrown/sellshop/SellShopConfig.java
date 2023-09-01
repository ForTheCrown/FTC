package net.forthecrown.sellshop;

import java.time.Duration;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ConfigSerializable
public class SellShopConfig {

  private String webstoreLink;

  private int defaultMaxEarnings;

  private Duration earningLossInterval;

  private int earningLoss;
}
