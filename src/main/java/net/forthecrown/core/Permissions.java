package net.forthecrown.core;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

/**
 * Class to store permission constants
 */
public final class Permissions {
  private Permissions() {}

  public static final String COMMAND_PREFIX = "ftc.commands.";
  public static final String PUNISH_PREFIX = "ftc.punish.";

  public static final Permission
      EAVESDROP               = register("ftc.eavesdrop"),
      EAVESDROP_ADMIN         = register(EAVESDROP.getName() + ".admin"),

      VANISH                  = register("ftc.vanish"),
      VANISH_SEE              = register(VANISH.getName() + ".see"),

      ADMIN                   = register("ftc.admin"),

      PUNISH_MUTE             = register(PUNISH_PREFIX + "mute"),
      PUNISH_SOFTMUTE         = register(PUNISH_PREFIX + "softmute"),
      PUNISH_JAIL             = register(PUNISH_PREFIX + "jail"),
      PUNISH_KICK             = register(PUNISH_PREFIX + "kick"),
      PUNISH_BAN              = register(PUNISH_PREFIX + "ban"),
      PUNISH_BANIP            = register(PUNISH_PREFIX + "banip"),
      PUNISH_NOTES            = register(PUNISH_PREFIX + "notes"),
      PUNISH_SEPARATE         = register(PUNISH_PREFIX + "separate"),

      STAFF_CHAT              = register("ftc.staffchat"),
      ROYAL_SWORD             = register("ftc.royalsword"),
      BROADCAST               = register("ftc.broadcast"),
      USABLES                 = register("ftc.usables"),

      CHAT_IGNORE_CASE        = register("ftc.chat.caseignore"),
      IGNORE_SWEARS           = register("ftc.chat.ignorebanned"),
      CHAT_EMOTES             = register("ftc.chat.emotes"),
      CHAT_COLORS             = register("ftc.chat.color"),
      CHAT_LINKS              = register("ftc.chat.links"),
      CHAT_CLEAN_LINKS        = registerPrefixed(CHAT_LINKS, "clean"),
      CHAT_GRADIENTS          = register("ftc.chat.gradients"),

      AUTO_SELL               = register("ftc.sellshop.auto"),

      WORLD_BYPASS            = register("ftc.teleport.worldbypass"),

      MARRY                   = register("ftc.marry"),

      EMOTES                  = register("ftc.emotes"),
      EMOTE_IGNORE            = registerPrefixed(EMOTES, "cooldown.ignore"),
      EMOTE_JINGLE            = registerPrefixed(EMOTES, "jingle"),
      EMOTE_POG               = registerPrefixed(EMOTES, "pog"),
      EMOTE_SCARE             = registerPrefixed(EMOTES, "scare"),
      EMOTE_HUG               = registerPrefixed(EMOTES, "hug"),

      MAIL                    = register("ftc.mail"),
      MAIL_OTHERS             = registerPrefixed(MAIL, "others"),
      MAIL_ALL                = registerPrefixed(MAIL, "all"),
      MAIL_ITEMS              = registerPrefixed(MAIL, "items"),

      SHOP_ADMIN              = register("ftc.shops.admin"),
      SHOP_EDIT               = registerCmd("shopedit"),
      SHOP_HISTORY            = registerCmd("shophistory"),

      NEARBY                  = registerCmd("near"),
      NEARBY_IGNORE           = registerPrefixed(NEARBY, "ignore"),
      NEARBY_ADMIN            = registerPrefixed(NEARBY, "admin"),

      WARP                    = registerCmd("warp"),
      KIT                     = registerCmd("kit"),
      WARP_ADMIN              = registerPrefixed(WARP, "admin"),
      KIT_ADMIN               = registerPrefixed(KIT, "admin"),

      PROFILE                 = registerCmd("profile"),
      PROFILE_BYPASS          = registerPrefixed(PROFILE, "bypass"),

      BACK                    = registerCmd("back"),

      TPA                     = registerCmd("tpa"),
      TPA_HERE                = registerCmd("tpahere"),

      HELP                    = registerCmd("help"),

      PAY                     = registerCmd("pay"),
      PAY_TOGGLE              = registerPrefixed(PAY, "toggle"),

      MESSAGE                 = registerCmd("message"),

      WORKBENCH               = registerCmd("workbench"),
      ENDER_CHEST             = registerCmd("enderchest"),
      STONE_CUTTER            = registerCmd("stonecutter"),
      GRINDSTONE              = registerCmd("grindstone"),
      SMITHING                = registerCmd("smithingtable"),
      CARTOGRAPHY             = registerCmd("cartography"),
      LOOM                    = registerCmd("loom"),

      REPAIR                  = registerCmd("repair"),

      DISPOSAL                = registerCmd("disposal"),
      FEED                    = registerCmd("feed"),
      HEAL                    = registerCmd("heal"),
      CMD_DUNGEONS            = registerCmd("dungeons"),

      HOME                    = registerCmd("home"),
      HOME_OTHERS             = registerPrefixed(HOME, "others"),

      BECOME_BARON            = registerCmd("becomebaron"),

      IGNORE                  = registerCmd("ignore"),
      IGNORELIST_OTHERS       = registerCmd("ignorelist.others"),
      IGNORE_AC               = registerCmd("ignoreac"),

      CMD_BEEZOOKA            = registerCmd("beezooka"),
      CMD_KITTY_CANNON        = registerCmd("kittycannon"),
      CMD_NICKNAME            = registerCmd("nickname"),
      CMD_SUICIDE             = registerCmd("suicide"),

      HAT                     = registerCmd("hat"),

      GUILD                   = register("ftc.guild"),
      GUILD_ADMIN             = registerPrefixed(GUILD, "admin"),

      CMD_GAMEMODE            = registerCmd("gamemode"),
      CMD_GAMEMODE_OTHERS     = registerPrefixed(CMD_GAMEMODE, "others"),
      CMD_GAMEMODE_CREATIVE   = registerPrefixed(CMD_GAMEMODE, "creative"),
      CMD_GAMEMODE_SPECTATOR  = registerPrefixed(CMD_GAMEMODE, "spectator"),
      CMD_GAMEMODE_ADVENTURE  = registerPrefixed(CMD_GAMEMODE, "adventure"),

      CMD_GET_POS             = registerCmd("getpos"),
      CMD_MEMORY              = registerCmd("memory"),

      CMD_TELEPORT            = registerCmd("teleport"),
      CMD_LIST                = registerCmd("list"),

      WAYPOINTS               = register("ftc.waypoints"),
      WAYPOINTS_ADMIN         = registerPrefixed(WAYPOINTS, "admin"),
      WAYPOINTS_FLAGS         = registerPrefixed(WAYPOINTS, "flags"),

      CHALLENGES              = register("ftc.challenges"),
      CHALLENGES_ADMIN        = registerPrefixed(CHALLENGES, "admin"),

      MARKETS                 = register("ftc.markets"),
      MARKET_WARNING          = registerPrefixed(MARKETS, "warning"),

      DEFAULT                 = register("ftc.default"),

      CLAIMING                = register("ftc.claiming"),
      CLAIM_ADMIN             = registerPrefixed(CLAIMING, "override_flags");

  public static final TieredPermission MAX_HOMES = TieredPermission.builder()
      .prefix("ftc.homes.")
      .allowUnlimited()
      .tiersFrom1To(5)
      .build();

  public static final TieredPermission TP_DELAY = TieredPermission.builder()
      .prefix("ftc.teleport.delay.")
      .unlimitedPerm("ftc.teleport.bypass")
      .allowUnlimited()
      .tiersFrom1To(5)
      .build();

  /**
   * Registers a string permission
   *
   * @param permission The name of the permission
   * @return The registered permission
   */
  public static Permission register(String permission) {
    PluginManager manager = Bukkit.getPluginManager();

    //Register the permission, or if it's already registered,
    //return the registered permission
    Permission perm = manager.getPermission(permission);
    if (perm == null) {
      manager.addPermission(perm = new Permission(permission));
    }

    return perm;
  }

  public static Permission registerCmd(String suffix) {
    return register(COMMAND_PREFIX + suffix);
  }

  public static Permission registerPrefixed(Permission parent, String suffix) {
    return register(parent.getName() + "." + suffix);
  }
}