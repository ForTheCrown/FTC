package net.forthecrown.economy;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@Getter @Setter
public class ShopConfig {

  private boolean logNormalUses = false;
  private boolean logAdminUses = true;
  private int maxPrice = 1000000;
  private Duration unloadDelay = Duration.ofMinutes(5);

  private Duration marketActionCooldown = Duration.ofDays(1);
  private boolean autoEvictEnabled = true;
  private Duration scanInterval = Duration.ofDays(1);
  private Duration evictionDelay = Duration.ofDays(14);
  private Duration inactiveKickTime = Duration.ofDays(28 * 2);
  private Duration unusedShopKickTime = Duration.ofDays(14);
  private int minimumShopAmount = 5;
  private int defaultPrice = 55000;
  private float minStock = 0.5f;
}
