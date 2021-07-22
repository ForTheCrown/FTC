package net.forthecrown.august.usables;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.august.AugustPlugin;
import net.forthecrown.august.EventUtil;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionEnterEvent implements UsageAction<ActionEnterEvent.ActionInstance> {
    private static final ActionInstance INSTANCE = new ActionInstance();
    public static final Key KEY = EventUtil.createEventKey("enter_event");

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return INSTANCE;
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return INSTANCE;
    }

    @Override
    public JsonElement serialize(ActionInstance instance) {
        return JsonNull.INSTANCE;
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class ActionInstance implements UsageActionInstance {

        @Override
        public void onInteract(Player player) {
            AugustPlugin.event.startHandled(player);
        }

        @Override
        public String asString() {
            return typeKey().asString() + "{}";
        }

        @Override
        public Key typeKey() {
            return KEY;
        }
    }
}
