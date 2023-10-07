package net.forthecrown.core;

import static net.forthecrown.Permissions.register;
import static net.forthecrown.Permissions.registerCmd;

import net.forthecrown.utils.TieredPermission;
import net.forthecrown.utils.TieredPermission.TierPriority;
import org.bukkit.permissions.Permission;

public interface CorePermissions {

  Permission IGNORE                  = registerCmd("ignore");
  Permission IGNORELIST              = registerCmd("ignorelist");
  Permission IGNORELIST_OTHERS       = register(IGNORELIST, "others");
  Permission IGNORE_BYPASS           = register(IGNORE, "bypass");

  Permission CMD_GAMEMODE            = registerCmd("gamemode");
  Permission CMD_GAMEMODE_OTHERS     = register(CMD_GAMEMODE, "others");
  Permission CMD_GAMEMODE_CREATIVE   = register(CMD_GAMEMODE, "creative");
  Permission CMD_GAMEMODE_SPECTATOR  = register(CMD_GAMEMODE, "spectator");
  Permission CMD_GAMEMODE_ADVENTURE  = register(CMD_GAMEMODE, "adventure");

  Permission CMD_INVSTORE            = registerCmd("invstore");
  Permission CMD_LAUNCH              = registerCmd("launch");
  Permission CMD_MEMORY              = registerCmd("memory");
  Permission CMD_TELEPORT            = registerCmd("teleport");
  Permission CMD_LIST                = registerCmd("list");
  Permission CMD_NICKNAME            = registerCmd("nickname");

  Permission WORKBENCH               = registerCmd("workbench");
  Permission ENDER_CHEST             = registerCmd("enderchest");
  Permission STONE_CUTTER            = registerCmd("stonecutter");
  Permission GRINDSTONE              = registerCmd("grindstone");
  Permission SMITHING                = registerCmd("smithingtable");
  Permission CARTOGRAPHY             = registerCmd("cartography");
  Permission LOOM                    = registerCmd("loom");

  Permission BACK                    = registerCmd("back");

  Permission NEARBY                  = registerCmd("near");
  Permission NEARBY_ADMIN            = register(NEARBY, "admin");
  Permission NEARBY_IGNORE           = register(NEARBY, "ignore");

  Permission CMD_BEEZOOKA            = registerCmd("beezooka");
  Permission CMD_KITTY_CANNON        = registerCmd("kittycannon");

  Permission MESSAGE                 = registerCmd("message");

  Permission HAT                     = registerCmd("hat");

  Permission GOD                     = register("ftc.godmode");
  Permission GOD_OTHERS              = register(GOD, "others");

  Permission FLY                     = register("ftc.flying");
  Permission FLY_OTHERS              = register(FLY, "others");

  Permission FEED                    = registerCmd("feed");
  Permission HEAL                    = registerCmd("heal");
  Permission DISPOSAL                = registerCmd("disposal");
  Permission REPAIR                  = registerCmd("repair");

  Permission PAY                     = registerCmd("pay");

  Permission IP_QUERY                = register("ftc.users.iplookup");

  Permission HOME                    = registerCmd("home");
  Permission HOME_OTHERS             = register(HOME, "others");

  TieredPermission MAX_HOMES = TieredPermission.builder()
      .prefix("ftc.homes.")
      .priority(TierPriority.HIGHEST)
      .allowUnlimited()
      .tiersBetween(1, 5)
      .build();
}
