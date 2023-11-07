package net.forthecrown.cosmetics;

import static net.forthecrown.Permissions.register;

import org.bukkit.permissions.Permission;

public interface CosmeticPermissions {

  Permission DEFAULT = register("ftc.cosmetics");
  Permission ADMIN = register(DEFAULT, "admin");
}
