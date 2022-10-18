package net.forthecrown.useables.test;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;

public final class UsageTests {
    private UsageTests() {}

    public static final String
            KEY_PERMISSION = "has_permission",
            KEY_SCORE = "has_score",
            KEY_NOT_ALT = "not_alt",
            KEY_EMPTY_INV = "inventory_empty",
            KEY_NO_RIDERS = "no_riders",
            KEY_HAS_ITEMS = "has_items",
            KEY_NOT_HAVE_ITEMS = "does_not_have_items",

            KEY_HAS_BAL = "rhines",
            KEY_HAS_GEMS = "gems",
            KEY_HAS_VOTES = "votes",

            KEY_NEVER_USED = "never_used",

            KEY_ONE_USE = "one_use",
            KEY_COOLDOWN = "cooldown",
            KEY_WORLD = "in_world",

            KEY_RANK = "rank";

    public static void init() {
        register(KEY_PERMISSION, TestPermission.TYPE);
        register(KEY_RANK, TestRank.TYPE);
        register(KEY_NOT_ALT, TestNotAlt.TYPE);
        register(KEY_NEVER_USED, TestNeverUsed.TYPE);
        register(KEY_ONE_USE, TestOneUse.TYPE);
        register(KEY_COOLDOWN, TestCooldown.TYPE);
        register(KEY_WORLD, TestWorld.TYPE);

        register(KEY_SCORE, TestHasScore.TYPE);
        register(KEY_EMPTY_INV, TestEmptyInventory.TYPE);
        register(KEY_NO_RIDERS, TestNoPassangers.TYPE);

        register(KEY_HAS_ITEMS, TestItems.TYPE_HAS);
        register(KEY_NOT_HAVE_ITEMS, TestItems.TYPE_HAS_NOT);

        register(KEY_HAS_BAL, TestUserMapValue.TYPE_BALANCE);
        register(KEY_HAS_GEMS, TestUserMapValue.TYPE_GEMS);
        register(KEY_HAS_VOTES, TestUserMapValue.TYPE_VOTES);

        Registries.USAGE_CHECKS.freeze();
    }

    private static void register(String key, UsageType<? extends UsageTest> type) {
        Registries.USAGE_CHECKS.register(key, type);
    }
}