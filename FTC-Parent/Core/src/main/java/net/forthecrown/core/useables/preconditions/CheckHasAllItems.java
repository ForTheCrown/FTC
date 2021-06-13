package net.forthecrown.core.useables.preconditions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.useables.UsageCheck;
import net.forthecrown.core.utils.JsonUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CheckHasAllItems implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.inst(), "has_all_items");

    private final List<ItemStack> items = new ArrayList<>();
    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        Player player = context.getSource().asPlayer();

        items.clear();
        for (ItemStack i: player.getInventory()){
            if(i == null) continue;

            items.add(i);
        }
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletionProvider.suggestMatching(builder, "-inventory");
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        items.clear();

        for (JsonElement e: json.getAsJsonArray()){
            items.add(JsonUtils.deserializeItem(e));
        }
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + '{' + "items=" + items.toString() + '}';
    }

    @Override
    public Component failMessage() {
        return Component.text("You don't have all the required items").color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        PlayerInventory inv = player.getInventory();

        for (ItemStack i: items){
            if(!inv.containsAtLeast(i, i.getAmount())) return false;
        }

        return true;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.serializeCollection(items, JsonUtils::serializeItem);
    }

    @Override
    public @NonNull Key key() {
        return KEY;
    }
}
