package net.forthecrown.useables.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CheckHasAllItems implements UsageCheck<CheckHasAllItems.CheckInstance> {
    public static final Key KEY = Keys.forthecrown("has_all_items");

    @Override
    public CheckInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        Player player = source.asPlayer();

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack i: player.getInventory()){
            if(ItemStacks.isEmpty(i)) continue;

            items.add(i.clone());
        }

        return new CheckInstance(items);
    }

    @Override
    public CheckInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new CheckInstance(ListUtils.fromIterable(element.getAsJsonArray(), JsonUtils::readItem));
    }

    @Override
    public JsonElement serialize(CheckInstance value) {
        return JsonUtils.writeCollection(value.getItems(), JsonUtils::writeItem);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public static class CheckInstance implements UsageCheckInstance {

        @Getter
        private final Collection<ItemStack> items;

        CheckInstance(Collection<ItemStack> items) {
            this.items = items;
        }

        @Override
        public String asString() {
            return typeKey().asString() + '{' + "items=" + items + '}';
        }

        @Override
        public Component failMessage(Player player) {
            final TextComponent.Builder builder = Component.text()
                    .color(NamedTextColor.YELLOW)
                    .content("You don't have the following items: ");

            for (ItemStack i: items) {
                boolean has = player.getInventory().containsAtLeast(i, i.getAmount());

                builder
                        .append(Component.newline())
                        .append(
                                Component.text()
                                        .color(has ? NamedTextColor.GRAY : NamedTextColor.GOLD)
                                        .append(Component.text("- "))
                                        .append(FtcFormatter.itemDisplayName(i))
                                        .build()
                        );
            }

            return builder.build();
        }

        @Override
        public @NotNull Key typeKey() {
            return KEY;
        }

        @Override
        public boolean test(Player player) {
            PlayerInventory inv = player.getInventory();

            for (ItemStack i: items){
                if(!inv.containsAtLeast(i, i.getAmount())) return false;
            }

            return true;
        }
    }
}