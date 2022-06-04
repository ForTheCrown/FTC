package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.InteractionUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionItem implements UsageAction<ActionItem.ActionInstance> {
    public static final NamespacedKey
            ADD_KEY     = Keys.forthecrown("give_item"),
            REMOVE_KEY  = Keys.forthecrown("remove_item");

    private final boolean add;

    public ActionItem(boolean add) {
        this.add = add;
    }

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(InteractionUtils.parseGivenItem(source, reader), add);
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(JsonUtils.readItem(element), add);
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return JsonUtils.writeItem(value.getItem());
    }

    @Override
    public @NotNull Key key() {
        return add ? ADD_KEY : REMOVE_KEY;
    }

    @Override
    public boolean requiresInput() {
        return false;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return InteractionUtils.listItems(context, builder);
    }

    public static class ActionInstance implements UsageActionInstance {
        private final ItemStack item;
        private final boolean add;

        public ActionInstance(ItemStack item, boolean add) {
            this.item = item;
            this.add = add;
        }

        public boolean isAdd() {
            return add;
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
        public Key typeKey() {
            return add ? ADD_KEY : REMOVE_KEY;
        }

        @Override
        public String asString() {
            return typeKey().asString() + "{" + ",item=" + item + '}';
        }

        public ItemStack getItem() {
            return item;
        }
    }
}