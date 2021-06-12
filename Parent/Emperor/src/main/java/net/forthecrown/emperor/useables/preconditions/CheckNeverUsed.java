package net.forthecrown.emperor.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;

public class CheckNeverUsed implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.inst(), "never_used");

    private boolean used = false;

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {}

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        this.used = json.getAsBoolean();
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + '{' + "used=" + used + '}';
    }

    @Override
    public Component failMessage() {
        return Component.text("First come, first serve").color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        return !used;
    }

    @Override
    public Consumer<Player> onSuccess() {
        return plr -> used = true;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(used);
    }

    @Override
    public @NonNull Key key() {
        return KEY;
    }
}
