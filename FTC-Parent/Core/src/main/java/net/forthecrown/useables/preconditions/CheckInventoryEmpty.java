package net.forthecrown.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.useables.UsageCheck;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckInventoryEmpty implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.inst(), "inventory_empty");

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {}
    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {}

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public String asString() {
        return key().asString();
    }

    @Override
    public Component failMessage() {
        return Component.text("You need to have an empty inventory").color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        return player.getInventory().isEmpty();
    }

    @Override
    public JsonElement serialize() {
        return JsonNull.INSTANCE;
    }
}
