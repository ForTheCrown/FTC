package net.forthecrown.core.types.interactable.actions;

import com.google.gson.JsonElement;
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
import net.minecraft.server.v1_16_R3.MojangsonParser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class ActionRemoveItem implements InteractionAction {
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
        player.getInventory().removeItemAnySlot(item);
    }

    @Override
    public String getRegistrationName() {
        return "remove_item";
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{item=" + item + "}";
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(NbtGetter.ofItem(item).serialize());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return InterUtils.listItems(context, builder);
    }
}
