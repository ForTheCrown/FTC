package net.forthecrown.emperor.useables.preconditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.nbt.NBT;
import net.forthecrown.emperor.nbt.NbtGetter;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.emperor.utils.InterUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class CheckHasItem implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.getNamespace(), "has_item");

    private ItemStack item;

    @Override
    public void parse(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        item = InterUtils.parseGivenItem(c, reader);
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        try {
            item = NbtGetter.itemFromNBT(NBT.of(MojangsonParser.parse(json.getAsString())));
        } catch (RuntimeException e) {
            item = null;
            e.printStackTrace();
        }
    }

    @Override
    public Key key() {
        return KEY;
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{item=" + item + "}";
    }

    @Override
    public Component getFailMessage() {
        return Component.text("You don't have the required item").color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        return player.getInventory().containsAtLeast(item, item.getAmount());
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(NbtGetter.ofItem(item).serialize());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return InterUtils.listItems(context, builder);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
