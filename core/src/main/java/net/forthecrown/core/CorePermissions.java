package net.forthecrown.core;

import static net.forthecrown.Permissions.register;
import static net.forthecrown.Permissions.registerCmd;

import org.bukkit.permissions.Permission;

public interface CorePermissions {

  Permission IGNORE                  = registerCmd("ignore");
  Permission IGNORELIST              = registerCmd("ignorelist");
  Permission IGNORELIST_OTHERS       = register(IGNORELIST, "others");

  Permission CMD_GAMEMODE            = registerCmd("gamemode");
  Permission CMD_GAMEMODE_OTHERS     = register(CMD_GAMEMODE, "others");
  Permission CMD_GAMEMODE_CREATIVE   = register(CMD_GAMEMODE, "creative");
  Permission CMD_GAMEMODE_SPECTATOR  = register(CMD_GAMEMODE, "spectator");
  Permission CMD_GAMEMODE_ADVENTURE  = register(CMD_GAMEMODE, "adventure");

  Permission CMD_INVSTORE            = registerCmd("invstore");
  Permission CMD_LAUNCH              = registerCmd("launch");
  Permission CMD_MEMORY              = registerCmd("memory");
  Permission CMD_TELEPORT            = registerCmd("teleport");

  Permission WORKBENCH               = registerCmd("workbench");
  Permission ENDER_CHEST             = registerCmd("enderchest");
  Permission STONE_CUTTER            = registerCmd("stonecutter");
  Permission GRINDSTONE              = registerCmd("grindstone");
  Permission SMITHING                = registerCmd("smithingtable");
  Permission CARTOGRAPHY             = registerCmd("cartography");
  Permission LOOM                    = registerCmd("loom");
}
