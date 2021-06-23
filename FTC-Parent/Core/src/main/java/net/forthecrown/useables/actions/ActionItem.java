package net.forthecrown.core.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.core.useables.UsageAction;
import net.forthecrown.utils.InterUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class ActionItem implements UsageAction {
    public static final Key ADD_KEY = Key.key(CrownCore.inst(), "give_item");
    public static final Key REMOVE_KEY = Key.key(CrownCore.inst(), "remove_item");

    private final boolean add;
    private ItemStack item;

    public ActionItem(boolean add){ this.add = add; }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        item = JsonUtils.deserializeItem(json);
    }

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        item = InterUtils.parseGivenItem(context, reader);
    }

    @Override
    public void onInteract(Player player) {
        if (item == null) return;

        if(add){
            if (player.getInventory().firstEmpty() == -1) player.getWorld().dropItem(player.getLocation(), item.clone());
            else player.getInventory().addItem(item.clone());
        } else player.getInventory().removeItemAnySlot(item);
    }

    @Override
    public Key key() {
        return add ? ADD_KEY : REMOVE_KEY;
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public JsonElement serialize() {
        if (item == null) return JsonNull.INSTANCE;

        return new JsonPrimitive(NbtHandler.ofItem(item).serialize());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return InterUtils.listItems(context, builder);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "action=" + (add ? "give" : "remove") + ",item=" + item + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionItem item1 = (ActionItem) o;

        return new EqualsBuilder()
                .append(item, item1.item)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(item)
                .toHashCode();
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
