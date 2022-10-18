package net.forthecrown.datafix;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.useables.*;
import net.forthecrown.useables.actions.UsageActions;
import net.forthecrown.useables.test.UsageTests;
import net.forthecrown.utils.io.JsonWrapper;

import java.util.Map;
import java.util.Optional;

public final class UsablesJsonReader {
    private UsablesJsonReader() {}

    private static final ImmutableMap<String, String> KEY_RENAMES = ImmutableMap.<String, String>builder()
            // Tests
            .put("required_gems", UsageTests.KEY_HAS_GEMS)
            .put("required_balance", UsageTests.KEY_HAS_BAL)
            .put("required_permission", UsageTests.KEY_PERMISSION)
            .put("one_use_per_user", UsageTests.KEY_ONE_USE)
            .put("required_rank", UsageTests.KEY_RANK)
            .put("has_item", UsageTests.KEY_HAS_ITEMS)

            // Actions
            .put("command_console", UsageActions.KEY_CMD_CONSOLE)
            .put("command_user", UsageActions.KEY_CMD_PLAYER)
            .put("teleport_user", UsageActions.KEY_TELEPORT)

            .put("show_boss_info", "boss_info")

            .build();

    public static UsageAction readAction(String strKey, JsonElement element) throws CommandSyntaxException {
        Registry reg = Registries.USAGE_ACTIONS;
        return (UsageAction) read(reg, strKey, element);
    }

    public static UsageTest readCheck(String strKey, JsonElement element) throws CommandSyntaxException {
        Registry reg = Registries.USAGE_CHECKS;
        return (UsageTest) read(reg, strKey, element);
    }

    private static <T extends UsageInstance> T read(Registry<UsageType<T>> registry,
                                                    String key,
                                                    JsonElement element
    ) throws CommandSyntaxException {
        key = Registry.removeNamespace(key);
        key = KEY_RENAMES.getOrDefault(key, key);

        Optional<UsageType<T>> valueOptional = registry.get(key);

        if (valueOptional.isEmpty()) {
            Crown.logger().warn("Could not find usage type with key: '{}'", key);
            return null;
        }

        return valueOptional.get().load(element);
    }

    public static void loadChecks(CheckHolder checkable, JsonObject json) throws CommandSyntaxException {
        var container = checkable.getChecks();

        JsonElement checks = json.get("preconditions");

        if (checks == null
                || !checks.isJsonObject()
        ) {
            return;
        }

        for (Map.Entry<String, JsonElement> e: checks.getAsJsonObject().entrySet()){
            var check = readCheck(e.getKey(), e.getValue());

            if (check == null) {
                continue;
            }

            container.add(check);
        }
    }

    public static void loadActions(ActionHolder actionable, JsonObject json) {
        var container = actionable.getActions();

        JsonElement actionsElement = json.get("actions");

        if (actionsElement == null
                || !actionsElement.isJsonArray()
        ) {
            return;
        }

        for (JsonElement e: actionsElement.getAsJsonArray()) {
            JsonWrapper j = JsonWrapper.wrap(e.getAsJsonObject());

            try {
                var action = readAction(j.getString("type"), j.get("value"));

                if (action == null) {
                    continue;
                }

                container.add(action);
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
    }
}