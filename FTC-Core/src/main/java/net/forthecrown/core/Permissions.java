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

    public static final Permission
            EAVESDROP               = register("ftc.eavesdrop"),
            EAVESDROP_DM            = register(EAVESDROP.getName() + ".dm"),
            EAVESDROP_SIGNS         = register(EAVESDROP.getName() + ".signs"),
            EAVESDROP_ADMIN         = register(EAVESDROP.getName() + ".admin"),
            EAVESDROP_MUTED         = register(EAVESDROP.getName() + ".muted"),
            EAVESDROP_MARRIAGE      = register(EAVESDROP.getName() + ".marriage"),

            VANISH                  = register("ftc.vanish"),
            VANISH_SEE              = register(VANISH.getName() + ".see"),

            HELPER                  = register("ftc.helper"),
            POLICE                  = register("ftc.police"),
            ADMIN                   = register("ftc.admin"),

            STAFF_CHAT              = register("ftc.staffchat"),
            ROYAL_SWORD             = register("ftc.royalsword"),
            GAMEMODES               = register("ftc.gamemodes"),
            KING_MAKER              = register("ftc.kingmaker"),
            BROADCAST               = register("ftc.broadcast"),

            IGNORE_CHAT_CASE        = register("ftc.chatcaseignore"),
            IGNORE_SWEARS           = register("ftc.ignorebannedwords"),

            DONATOR_1               = register("ftc.donator1"),
            DONATOR_2               = register("ftc.donator2"),
            DONATOR_3               = register("ftc.donator3"),

            TP_BYPASS               = register("ftc.teleports.bypass"),
            WORLD_BYPASS            = register("ftc.teleports.worldbypass"),

            MARRY                   = register("ftc.marry"),

            EMOTES                  = register("ftc.emotes"),
            EMOTE_IGNORE            = register(EMOTES.getName() + ".cooldown.ignore"),
            EMOTE_JINGLE            = register(EMOTES.getName() + ".jingle"),
            EMOTE_POG               = register(EMOTES.getName() + ".pog"),
            EMOTE_SCARE             = register(EMOTES.getName() + ".scare"),
            EMOTE_HUG               = register(EMOTES.getName() + ".hug"),

            MAIL                    = register("ftc.mail"),
            MAIL_OTHERS             = register(MAIL.getName() + ".others"),
            MAIL_ALL                = register(MAIL.getName() + ".all"),

            GUILD                   = register("ftc.guilds"),
            GUILD_ADMIN             = register(GUILD.getName() + ".admin"),

            SHOP_ADMIN              = register("ftc.shops.admin"),
            SHOP_EDIT               = register(COMMAND_PREFIX + "shopedit"),
            SHOP_HISTORY            = register(COMMAND_PREFIX + "shophistory"),

            NEARBY                  = register(COMMAND_PREFIX + "near"),
            NEARBY_IGNORE           = register(COMMAND_PREFIX + "near.ignore"),

            WARP                    = register(COMMAND_PREFIX + "warp"),
            WARP_ADMIN              = register(COMMAND_PREFIX + "warp.admin"),

            KIT                     = register(COMMAND_PREFIX + "kit"),
            KIT_ADMIN               = register(COMMAND_PREFIX + "kit.admin"),

            PROFILE                 = register(COMMAND_PREFIX + "profile"),
            PROFILE_BYPASS          = register(COMMAND_PREFIX + "profile.bypass"),

            BACK                    = register(COMMAND_PREFIX + "back"),

            TPA                     = register(COMMAND_PREFIX + "tpa"),
            TPA_HERE                = register(COMMAND_PREFIX + "tpahere"),

            HELP                    = register(COMMAND_PREFIX + "help"),

            PAY_TOGGLE              = register(COMMAND_PREFIX + "pay.toggle"),
            PAY                     = register(COMMAND_PREFIX + "pay"),

            MESSAGE                 = register(COMMAND_PREFIX + "message"),

            WORKBENCH               = register(COMMAND_PREFIX + "workbench"),
            WORKBENCH_OTHERS        = register(COMMAND_PREFIX + "workbench.others"),

            ENDER_CHEST             = register(COMMAND_PREFIX + "enderchest"),
            ENDER_CHEST_OTHERS      = register(COMMAND_PREFIX + "enderchest.others"),

            STONE_CUTTER            = register(COMMAND_PREFIX + "stonecutter"),
            STONE_CUTTER_OTHERS     = register(COMMAND_PREFIX + "stonecutter.others"),

            GRINDSTONE              = register(COMMAND_PREFIX + "grindstone"),
            GRINDSTONE_OTHERS       = register(COMMAND_PREFIX + "grindstone.others"),

            SMITHING                = register(COMMAND_PREFIX + "smithingtable"),
            SMITHING_OTHERS         = register(COMMAND_PREFIX + "smithingtable.others"),

            CARTOGRAPHY             = register(COMMAND_PREFIX + "cartography"),
            CARTOGRAPHY_OTHERS      = register(COMMAND_PREFIX + "cartography.others"),

            REPAIR                  = register(COMMAND_PREFIX + "repair"),

            LOOM                    = register(COMMAND_PREFIX + "loom"),
            LOOM_OTHERS             = register(COMMAND_PREFIX + "loom.others"),

            DISPOSAL                = register(COMMAND_PREFIX + "disposal"),
            FEED                    = register(COMMAND_PREFIX + "feed"),
            HEAL                    = register(COMMAND_PREFIX + "heal"),

            HOME                    = register(COMMAND_PREFIX + "home"),
            HOME_OTHERS             = register(COMMAND_PREFIX + "home.others"),

            BECOME_BARON            = register(COMMAND_PREFIX + "becomebaron"),

            IGNORE                  = register(COMMAND_PREFIX + "ignore"),
            IGNORE_AC               = register(COMMAND_PREFIX + "ignoreac"),

            REGIONS                 = register("ftc.regions"),
            REGIONS_ADMIN           = register(REGIONS.getName() + ".admin"),

            MARKETS                 = register("ftc.markets"),

            DEFAULT                 = register("ftc.default");

    /**
     * Registers a string permission
     * @param permission The name of the permission
     * @return The registered permission
     */
    public static Permission register(String permission){
        PluginManager manager = Bukkit.getPluginManager();

        //Register the permission, or if it's already registered,
        //return the registered permission
        Permission perm = manager.getPermission(permission);
        if(perm == null) manager.addPermission(perm = new Permission(permission));

        return perm;
    }
}