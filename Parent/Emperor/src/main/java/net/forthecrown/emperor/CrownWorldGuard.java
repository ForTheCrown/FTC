package net.forthecrown.emperor;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

/**
 * Creates and registers flags for this plugin
 */
public final class CrownWorldGuard {

    public static final BranchFlag SHOP_USAGE_FLAG = new BranchFlag("shop-usage");
    public static final BranchFlag SHOP_OWNERSHIP_FLAG = new BranchFlag("shop-ownership");

    public static final StateFlag SHOP_CREATION = new StateFlag("shop-creation", true);
    public static final StateFlag TRAPDOOR_USE = new StateFlag("trapdoor-use", true);

    public static void init(){
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(SHOP_OWNERSHIP_FLAG);
            registry.register(SHOP_USAGE_FLAG);
            registry.register(SHOP_CREATION);
            registry.register(TRAPDOOR_USE);

            CrownCore.logger().info("Core's flags registered");
        } catch (FlagConflictException e){
            e.printStackTrace();
        }
    }
}
