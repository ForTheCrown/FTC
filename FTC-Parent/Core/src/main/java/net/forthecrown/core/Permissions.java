package net.forthecrown.core;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

/**
 * Class to store permission constants
 */
public final class Permissions {
    public static final String COMMAND_PREFIX = "ftc.commands.";

    public static final Permission EAVESDROP_ADMIN =    register("ftc.eavesdrop.admin");
    public static final Permission EAVESDROP =          register("ftc.eavesdrop");
    public static final Permission EAVESDROP_DM =       register("ftc.eavesdrop.dm");
    public static final Permission EAVESDROP_SIGNS =    register("ftc.eavesdrop.signs");
    public static final Permission EAVESDROP_MARRIAGE = register("ftc.eavesdrop.marriage");
    public static final Permission EAVESDROP_MUTED =    register("ftc.eavesdrop.muted");

    public static final Permission HELPER =             register("ftc.helper");
    public static final Permission POLICE =             register("ftc.police");

    public static final Permission MUTE_BYPASS =        register("ftc.mute.bypass");
    public static final Permission KICK_BYPASS =        register("ftc.kick.bypass");
    public static final Permission BAN_BYPASS =         register("ftc.ban.bypass");
    public static final Permission JAIL_BYPASS =        register("ftc.jail.bypass");

    public static final Permission VANISH =             register("ftc.vanish");
    public static final Permission VANISH_SEE =         register("ftc.vanish.see");

    public static final Permission STAFF_CHAT =         register("ftc.staffchat");
    public static final Permission CORE_ADMIN =         register("ftc.admin");
    public static final Permission SHOP_ADMIN =         register("ftc.shops.admin");
    public static final Permission GAMEMODES =          register("ftc.gamemodes");
    public static final Permission KING_MAKER =         register("ftc.kingmaker");
    public static final Permission IGNORE_CHAT_CASE =   register("ftc.chatcaseignore");
    public static final Permission BROADCAST =          register("ftc.broadcast");

    public static final Permission DONATOR_1 =          register("ftc.donator1");
    public static final Permission DONATOR_2 =          register("ftc.donator2");
    public static final Permission DONATOR_3 =          register("ftc.donator3");

    public static final Permission TP_BYPASS =          register("ftc.teleports.bypass");
    public static final Permission WORLD_BYPASS =       register("ftc.teleports.worldbypass");

    public static final Permission MARRY =              register("ftc.marry");
    public static final Permission MARRY_PEOPLE =       register("ftc.marry.others"); //Make 2 people marry each other.

    public static final Permission WARP =               register(COMMAND_PREFIX + "warp");
    public static final Permission WARP_ADMIN =         register(COMMAND_PREFIX + "warp.admin");

    public static final Permission KIT =                register(COMMAND_PREFIX + "kit");
    public static final Permission KIT_ADMIN =          register(COMMAND_PREFIX + "kit.admin");

    public static final Permission PROFILE =            register(COMMAND_PREFIX + "profile");
    public static final Permission PROFILE_BYPASS =     register(COMMAND_PREFIX + "profile.bypass");

    public static final Permission BACK =               register(COMMAND_PREFIX + "back");

    public static final Permission TPA =                register(COMMAND_PREFIX + "tpa");
    public static final Permission TPA_HERE =           register(COMMAND_PREFIX + "tpahere");

    public static final Permission HELP =               register(COMMAND_PREFIX + "help");

    public static final Permission MESSAGE =            register(COMMAND_PREFIX + "message");

    public static final Permission WORKBENCH =          register(COMMAND_PREFIX + "workbench");
    public static final Permission WORKBENCH_OTHERS =   register(COMMAND_PREFIX + "workbench.others");

    public static final Permission ENDER_CHEST =        register(COMMAND_PREFIX + "enderchest");
    public static final Permission ENDER_CHEST_OTHERS = register(COMMAND_PREFIX + "enderchest.others");

    public static final Permission STONE_CUTTER =       register(COMMAND_PREFIX + "stonecutter");
    public static final Permission STONE_CUTTER_OTHERS =register(COMMAND_PREFIX + "stonecutter.others");

    public static final Permission GRINDSTONE =         register(COMMAND_PREFIX + "grindstone");
    public static final Permission GRINDSTONE_OTHERS =  register(COMMAND_PREFIX + "grindstone.others");

    public static final Permission SMITHING =           register(COMMAND_PREFIX + "smithingtable");
    public static final Permission SMITHING_OTHERS =    register(COMMAND_PREFIX + "smithingtable.others");

    public static final Permission CARTOGRAPHY =        register(COMMAND_PREFIX + "cartography");
    public static final Permission CARTOGRAPHY_OTHERS = register(COMMAND_PREFIX + "cartography.others");

    public static final Permission REPAIR =             register(COMMAND_PREFIX + "repair");

    public static final Permission LOOM =               register(COMMAND_PREFIX + "loom");
    public static final Permission LOOM_OTHERS =        register(COMMAND_PREFIX + "loom.others");

    public static final Permission DISPOSAL =           register(COMMAND_PREFIX + "disposal");
    public static final Permission FEED =               register(COMMAND_PREFIX + "feed");
    public static final Permission HEAL =               register(COMMAND_PREFIX + "heal");

    public static final Permission HOME =               register(COMMAND_PREFIX + "home");
    public static final Permission HOME_OTHERS =        register(COMMAND_PREFIX + "home.others");

    public static final Permission BECOME_BARON =       register(COMMAND_PREFIX + "becomebaron");

    public static final Permission DEFAULT =            register("ftc.default");

    /**
     * Registers a string permission
     * @param permission The name of the permission
     * @return The registered permission
     */
    public static Permission register(String permission){
        Permission perm = new Permission(permission);

        PluginManager pm = Bukkit.getPluginManager();
        if(pm.getPermission(permission) == null) pm.addPermission(perm);

        return perm;
    }
}
