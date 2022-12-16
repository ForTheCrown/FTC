package net.forthecrown.core;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Location;

/**
 * Creates and registers flags for this plugin
 */
public final class FtcFlags {
    private FtcFlags() {}

    public static final StateFlag
            SHOP_CREATION   = new StateFlag("shop-creation", true),
            TRAPDOOR_USE    = new StateFlag("trapdoor-use", true);

    static void init() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            registry.register(SHOP_CREATION);
            registry.register(TRAPDOOR_USE);
        } catch (FlagConflictException e){
            e.printStackTrace();
        }
    }

    public static <T> T query(Location pos, Flag<T> flag) {
        return WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer().createQuery()
                .queryValue(BukkitAdapter.adapt(pos), null, flag);
    }
}