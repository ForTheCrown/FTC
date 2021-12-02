package net.forthecrown.useables.checks;

import net.forthecrown.core.Crown;

import static net.forthecrown.registry.Registries.USAGE_CHECKS;

/**
 * Used for instantiating and registering and the default usage checks
 */
public final class UsageChecks {
    private UsageChecks() {}

    public static void init(){
        register(new CheckCooldown());
        register(new CheckHasAllItems());

        register(new CheckHasitem());
        register(new CheckHasScore());

        register(new CheckInWorld());

        register(new CheckNeverUsed());
        register(new CheckNotUsedBefore());

        register(new CheckRank());
        register(new CheckPermission());

        register(new CheckNumber(true));
        register(new CheckNumber(false));

        register(new SimpleCheckType(CheckInventoryEmpty::new, CheckInventoryEmpty.KEY));
        register(new SimpleCheckType(CheckNoRiders::new, CheckNoRiders.KEY));
        register(new SimpleCheckType(CheckIsNotAlt::new, CheckIsNotAlt.KEY));

        USAGE_CHECKS.close();
        Crown.logger().info("Default checks registered");
    }

    private static void register(UsageCheck<?> check){
        USAGE_CHECKS.register(check.key(), check);
    }
}
