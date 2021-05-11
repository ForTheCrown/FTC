package net.forthecrown.core.types.interactable.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.core.types.interactable.InteractionAction;
import net.forthecrown.core.utils.InterUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.item.ItemArgument;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class ActionGiveItem implements InteractionAction {
    private ItemStack item;

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
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        item = InterUtils.parseGivenItem(context, reader);
    }

    @Override
    public void onInteract(Player player) {
        if (item == null) return;

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), item.clone());
        } else {
            player.getInventory().addItem(item.clone());
        }
    }

    @Override
    public String getRegistrationName() {
        return "give_item";
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public JsonElement serialize() {
        if (item == null) return JsonNull.INSTANCE;

        return new JsonPrimitive(NbtGetter.ofItem(item).serialize());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return ItemArgument.itemStack().listSuggestions(context, builder);
    }

    @Override
    public String toString() {
        return  getClass().getSimpleName() + "{" + "item=" + item + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionGiveItem item1 = (ActionGiveItem) o;

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
}
