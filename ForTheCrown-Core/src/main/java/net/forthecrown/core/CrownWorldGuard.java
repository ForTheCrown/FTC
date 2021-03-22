package net.forthecrown.core;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public final class CrownWorldGuard {

    private final FtcCore core;

    CrownWorldGuard(FtcCore core){
        this.core = core;
    }

    public static final BranchFlag SHOP_USAGE_FLAG = new BranchFlag("shop-usage");
    public static final BranchFlag SHOP_OWNERSHIP_FLAG = new BranchFlag("shop-ownership");
    public static final StateFlag SHOP_CREATION = new StateFlag("shop-creation", true);

    public void registerFlags(){
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(SHOP_OWNERSHIP_FLAG);
            registry.register(SHOP_USAGE_FLAG);
            registry.register(SHOP_CREATION);
        } catch (FlagConflictException e){
            e.printStackTrace();
        }
    }
}
