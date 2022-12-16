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

            TP_BYPASS               = register("ftc.teleports.bypass"),
            WORLD_BYPASS            = register("ftc.teleports.worldbypass"),

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
            SHOP_EDIT               = register(COMMAND_PREFIX + "shopedit"),
            SHOP_HISTORY            = register(COMMAND_PREFIX + "shophistory"),

            NEARBY                  = register(COMMAND_PREFIX + "near"),
            NEARBY_IGNORE           = registerPrefixed(NEARBY, "ignore"),
            NEARBY_ADMIN            = registerPrefixed(NEARBY, "admin"),

            WARP                    = register(COMMAND_PREFIX + "warp"),
            KIT                     = register(COMMAND_PREFIX + "kit"),
            WARP_ADMIN              = registerPrefixed(WARP, "admin"),
            KIT_ADMIN               = registerPrefixed(KIT,  "admin"),

            PROFILE                 = register(COMMAND_PREFIX + "profile"),
            PROFILE_BYPASS          = registerPrefixed(PROFILE, "bypass"),

            BACK                    = register(COMMAND_PREFIX + "back"),

            TPA                     = register(COMMAND_PREFIX + "tpa"),
            TPA_HERE                = register(COMMAND_PREFIX + "tpahere"),

            HELP                    = register(COMMAND_PREFIX + "help"),

            PAY                     = register(COMMAND_PREFIX + "pay"),
            PAY_TOGGLE              = registerPrefixed(PAY, "toggle"),

            MESSAGE                 = register(COMMAND_PREFIX + "message"),

            WORKBENCH               = register(COMMAND_PREFIX + "workbench"),
            ENDER_CHEST             = register(COMMAND_PREFIX + "enderchest"),
            STONE_CUTTER            = register(COMMAND_PREFIX + "stonecutter"),
            GRINDSTONE              = register(COMMAND_PREFIX + "grindstone"),
            SMITHING                = register(COMMAND_PREFIX + "smithingtable"),
            CARTOGRAPHY             = register(COMMAND_PREFIX + "cartography"),
            LOOM                    = register(COMMAND_PREFIX + "loom"),

            REPAIR                  = register(COMMAND_PREFIX + "repair"),

            DISPOSAL                = register(COMMAND_PREFIX + "disposal"),
            FEED                    = register(COMMAND_PREFIX + "feed"),
            HEAL                    = register(COMMAND_PREFIX + "heal"),
            CMD_DUNGEONS            = register(COMMAND_PREFIX + "dungeons"),

            HOME                    = register(COMMAND_PREFIX + "home"),
            HOME_OTHERS             = registerPrefixed(HOME, "others"),

            BECOME_BARON            = register(COMMAND_PREFIX + "becomebaron"),

            IGNORE                  = register(COMMAND_PREFIX + "ignore"),
            IGNORELIST_OTHERS       = register(COMMAND_PREFIX + "ignorelist.others"),
            IGNORE_AC               = register(COMMAND_PREFIX + "ignoreac"),

            CMD_BEEZOOKA            = register(COMMAND_PREFIX + "beezooka"),
            CMD_KITTY_CANNON        = register(COMMAND_PREFIX + "kittycannon"),
            CMD_NICKNAME            = register(COMMAND_PREFIX + "nickname"),
            CMD_SUICIDE             = register(COMMAND_PREFIX + "suicide"),

            HAT                     = register(COMMAND_PREFIX + "hat"),

            GUILD                   = register("ftc.guild"),
            GUILD_ADMIN             = registerPrefixed(GUILD, "admin"),

            CMD_GAMEMODE            = register(COMMAND_PREFIX + "gamemode"),
            CMD_GAMEMODE_OTHERS     = registerPrefixed(CMD_GAMEMODE, "others"),
            CMD_GAMEMODE_CREATIVE   = registerPrefixed(CMD_GAMEMODE, "creative"),
            CMD_GAMEMODE_SPECTATOR  = registerPrefixed(CMD_GAMEMODE, "spectator"),
            CMD_GAMEMODE_ADVENTURE  = registerPrefixed(CMD_GAMEMODE, "adventure"),

            CMD_GET_POS             = register(COMMAND_PREFIX + "getpos"),
            CMD_MEMORY              = register(COMMAND_PREFIX + "memory"),

            CMD_TELEPORT            = register(COMMAND_PREFIX + "teleport"),
            CMD_LIST                = register(COMMAND_PREFIX + "list"),

            WAYPOINTS               = register("ftc.waypoints"),
            WAYPOINTS_ADMIN         = registerPrefixed(WAYPOINTS, "admin"),
            WAYPOINTS_FLAGS         = registerPrefixed(WAYPOINTS, "flags"),

            CHALLENGES              = register("ftc.challenges"),
            CHALLENGES_ADMIN        = registerPrefixed(CHALLENGES, "admin"),

            MARKETS                 = register("ftc.markets"),
            MARKET_WARNING          = registerPrefixed(MARKETS, "warning"),

            DEFAULT                 = register("ftc.default");

    public static final TieredPermission MAX_HOMES = TieredPermission.builder()
            .prefix("ftc.homes.")
            .allowUnlimited()
            .tiersFrom1To(5)
            .build();

    /**
     * Registers a string permission
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

    public static Permission registerPrefixed(Permission parent, String suffix) {
        return register(parent.getName() + "." + suffix);
    }
}