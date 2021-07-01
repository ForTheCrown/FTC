package net.forthecrown.useables.preconditions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.UsageCheck;
import net.forthecrown.utils.InterUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.TagParser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class CheckHasItem implements UsageCheck {
    public static final Key KEY = Key.key(CrownCore.inst(), "has_item");

    private ItemStack item;

    @Override
    public void parse(CommandContext<CommandSource> c, StringReader reader) throws CommandSyntaxException {
        item = InterUtils.parseGivenItem(c, reader);
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        try {
            item = NbtHandler.itemFromNBT(NBT.of(TagParser.parseTag(json.getAsString())));
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
    public Component failMessage() {
        return Component.text("You don't have the required item").color(NamedTextColor.GRAY);
    }

    @Override
    public boolean test(Player player) {
        return player.getInventory().containsAtLeast(item, item.getAmount());
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeItem(item);
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