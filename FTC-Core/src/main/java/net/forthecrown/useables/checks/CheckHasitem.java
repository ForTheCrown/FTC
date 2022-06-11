package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.UsablesUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CheckHasitem implements UsageCheck<CheckHasitem.CheckInstance> {
    public static Key KEY = Keys.forthecrown("has_item");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new CheckInstance(UsablesUtils.parseGivenItem(source, reader));
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(JsonUtils.readItem(element));
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return JsonUtils.writeItem(value.getItem());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return UsablesUtils.listItems(context, builder);
    }

    @Override
    public boolean requiresInput() {
        return false;
    }

    @RequiredArgsConstructor
    public static class CheckInstance implements UsageCheckInstance {
        @Getter
        private final ItemStack item;

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "item=" + item + '}';
        }

        @Override
        public Component failMessage(Player player) {
            return Component.text("You don't have the required item: ")
                    .append(FtcFormatter.itemDisplayName(item))
                    .color(NamedTextColor.GRAY);
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }

        @Override
        public boolean test(Player player) {
            return player.getInventory().containsAtLeast(getItem(), getItem().getAmount());
        }
    }
}