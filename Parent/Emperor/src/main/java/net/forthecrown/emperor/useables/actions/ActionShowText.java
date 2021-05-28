package net.forthecrown.emperor.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.useables.UsageAction;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.entity.Player;

public class ActionShowText implements UsageAction {
    public static final Key KEY = Key.key(CrownCore.getNamespace(), "show_text");

    private static final GsonComponentSerializer serializer = GsonComponentSerializer.gson();
    private Component component;

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        try {
            component = serializer.deserializeFromTree(json);
        } catch (Exception e) {
            component = null;
        }
    }

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        try {
            component = serializer.deserialize(reader.getString());
        } catch (Exception e) {
            throw FtcExceptionProvider.create(e.getMessage());
        }
    }

    @Override
    public void onInteract(Player player) {
        if (component == null) return;
        player.sendMessage(component);
    }

    @Override
    public Key key() {
        return KEY;
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public JsonElement serialize() {
        if (component == null) return new JsonObject();

        return serializer.serializeToTree(component);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "component=" + serializer.serialize(component) + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ActionShowText text = (ActionShowText) o;

        return new EqualsBuilder()
                .append(component, text.component)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(component)
                .toHashCode();
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}
