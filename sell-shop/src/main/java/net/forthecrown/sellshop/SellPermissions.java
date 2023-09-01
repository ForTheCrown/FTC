package net.forthecrown.sellshop;

import net.forthecrown.Permissions;
import org.bukkit.permissions.Permission;

public interface SellPermissions {

  Permission SELL_SHOP = Permissions.register("ftc.sellshop");
  Permission SHOP_ADMIN = Permissions.register(SELL_SHOP, "admin");
  Permission AUTO_SELL = Permissions.register(SELL_SHOP, "auto");
}
