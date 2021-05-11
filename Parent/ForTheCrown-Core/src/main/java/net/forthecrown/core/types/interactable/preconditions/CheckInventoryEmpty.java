package net.forthecrown.core.types.interactable.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.types.interactable.InteractionCheck;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class CheckInventoryEmpty implements InteractionCheck {
    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {}
    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {}

    @Override
    public String getRegistrationName() {
        return "inventory_empty";
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{}";
    }

    @Override
    public Component getFailMessage() {
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
