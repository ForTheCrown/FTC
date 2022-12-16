package net.forthecrown.useables.actions;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;

public final class UsageActions {
    private UsageActions() {}

    public static final String
            KEY_SHOW_TEXT    = "show_text",
            KEY_ADD_ITEM     = "give_item",
            KEY_REMOVE_ITEM  = "remove_item",
            KEY_CMD_CONSOLE  = "console_command",
            KEY_CMD_PLAYER   = "player_command",
            KEY_HOLIDAY_ITEM = "give_holiday_item",
            KEY_TELEPORT     = "teleport",
            KEY_SCRIPT       = "run_script";

    public static void init() {
        register(KEY_SHOW_TEXT, ActionShowText.TYPE);
        register(KEY_HOLIDAY_ITEM, ActionHolidayItem.TYPE);
        register(KEY_TELEPORT, ActionTeleport.TYPE);

        register(KEY_ADD_ITEM, ActionItem.TYPE_ADD);
        register(KEY_REMOVE_ITEM, ActionItem.TYPE_REM);

        register(KEY_CMD_CONSOLE, ActionCommand.TYPE_SERVER);
        register(KEY_CMD_PLAYER,  ActionCommand.TYPE_PLAYER);

        register(KEY_SCRIPT, ActionScript.TYPE);

        ActionScore.Action.registerAll();
        ActionUserMap.Type.registerAll();

        Registries.USAGE_ACTIONS.freeze();
    }

    private static void register(String key, UsageType<? extends UsageAction> type) {
        Registries.USAGE_ACTIONS.register(key, type);
    }
}