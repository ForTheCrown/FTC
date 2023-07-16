package net.forthecrown.cosmetics;

import net.forthecrown.Permissions;
import org.bukkit.permissions.Permission;

public interface CosmeticPermissions {

  Permission DEFAULT = Permissions.register("ftc.cosmetics");
  Permission ADMIN = Permissions.register(DEFAULT, "admin");
}
