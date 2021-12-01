package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionShowText implements UsageAction<ActionShowText.ActionInstance> {
    public static final Key KEY = Keys.ftccore("show_text");
    private static final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(ComponentArgument.component().parse(reader));
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(serializer.deserializeFromTree(element));
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return serializer.serializeToTree(value.getComponent());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class ActionInstance implements UsageActionInstance {
        private final Component component;

        public ActionInstance(Component component) {
            this.component = component;
        }

        @Override
        public void onInteract(Player player) {
            if (component == null) return;
            player.sendMessage(component);
        }

        @Override
        public Key typeKey() {
            return KEY;
        }

        @Override
        public String asString() {
            return typeKey().asString() + "{" + "component=" + serializer.serialize(component) + '}';
        }

        public Component getComponent() {
            return component;
        }
    }
}
