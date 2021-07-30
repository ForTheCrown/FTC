package net.forthecrown.useables.preconditions;

import net.forthecrown.core.ForTheCrown;

import static net.forthecrown.registry.Registries.USAGE_CHECKS;

/**
 * Used for instantiating and registering and the default usage checks
 */
public class UsageChecks {
    public static void init(){
        register(new CheckCooldownType());
        register(new CheckHasAllItemsType());

        register(new CheckHasItemType());
        register(new CheckHasScoreType());

        register(new CheckInWorld());

        register(new CheckNeverUsed());
        register(new CheckNotUsedBefore());

        register(new CheckRankType());
        register(new CheckBranchType());
        register(new CheckPermission());

        register(new CheckNumber(true));
        register(new CheckNumber(false));

        register(new SimpleCheckType(CheckInventoryEmpty::new, CheckInventoryEmpty.KEY));
        register(new SimpleCheckType(CheckIsNotAlt::new, CheckIsNotAlt.KEY));

        USAGE_CHECKS.close();
        ForTheCrown.logger().info("Default checks registered");
    }

    private static void register(UsageCheck<?> check){
        USAGE_CHECKS.register(check.key(), check);
    }
}
