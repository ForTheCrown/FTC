package net.forthecrown.usables;

import static net.forthecrown.Permissions.register;

import org.bukkit.permissions.Permission;

public interface UPermissions {

  Permission USABLES = register("ftc.usables");

  Permission ADMIN_INTERACTION = register(USABLES, "adminuse");

  Permission ENTITY = register(USABLES, "entity");
  Permission BLOCK = register(USABLES, "block");
  Permission ITEM = register(USABLES, "item");
  Permission TRIGGER = register(USABLES, "trigger");

  Permission WARP = register("ftc.warps");
  Permission WARP_ADMIN = register(WARP, "admin");

  Permission KIT = register("ftc.kits");
  Permission KIT_ADMIN = register(KIT, "admin");
}
