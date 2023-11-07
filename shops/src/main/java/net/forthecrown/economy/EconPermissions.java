package net.forthecrown.economy;

import net.forthecrown.Permissions;
import org.bukkit.permissions.Permission;

public interface EconPermissions {

  Permission SHOP_ADMIN = Permissions.register("ftc.signshops.admin");
  Permission SHOP_EDIT  = Permissions.registerCmd("shopedit");
  Permission SHOP_HISTORY  = Permissions.registerCmd("shophistory");

  Permission MARKETS = Permissions.register("ftc.markets");
  Permission MARKETS_ADMIN = Permissions.register(MARKETS, "admin");
}
