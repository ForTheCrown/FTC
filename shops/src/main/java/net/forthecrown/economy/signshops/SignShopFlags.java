package net.forthecrown.economy.signshops;

import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.forthecrown.Loggers;

public class SignShopFlags {

  public static final StateFlag SHOP_CREATION = new StateFlag("shop-creation", true);

  public static void register(FlagRegistry registry) {
    try {
      registry.register(SHOP_CREATION);
    } catch (FlagConflictException exc) {
      Loggers.getLogger().error("Error registering flag {}: {}",
          SHOP_CREATION.getName(), exc.getMessage()
      );
    }
  }
}
