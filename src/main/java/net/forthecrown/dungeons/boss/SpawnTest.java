package net.forthecrown.dungeons.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface SpawnTest {
    default boolean test(Player player) {
        if(!canSpawn(player)) {
            player.sendMessage(denyMessage(player));
            return false;
        }

        return true;
    }

    boolean canSpawn(Player player);
    Component denyMessage(@Nullable Player player);

    static Items items(ItemStack... items) {
        return new Items(ArrayUtils.isEmpty(items) ? List.of() : Arrays.asList(items));
    }

    static Items items(Collection<ItemStack> itemStacks) {
        return new Items(itemStacks);
    }

    @Getter
    @RequiredArgsConstructor
    class Items implements SpawnTest {
        private final Collection<ItemStack> items;

        @Override
        public boolean canSpawn(Player player) {
            PlayerInventory inventory = player.getInventory();
            List<ItemStack> items = new ObjectArrayList<>();

            for (ItemStack i: getItems()) {
                if (!inventory.containsAtLeast(i, i.getAmount())) {
                    return false;
                }

                PlayerMoveEvent.getHandlerList().bake();;

                // I do not trust non cloned item stacks
                items.add(i.clone());
            }

            // Remove all items
            inventory.removeItemAnySlot(items.toArray(ItemStack[]::new));
            return true;
        }

        @Override
        public Component denyMessage(@Nullable Player player) {
            PlayerInventory inventory = player == null ? null : player.getInventory();

            TextComponent.Builder text = Component.text()
                    .color(NamedTextColor.AQUA)
                    .append(Component.text("To spawn the boss in this level, you need:"));

            for (ItemStack i: getItems()) {
                TextColor color = inventory == null || inventory.containsAtLeast(i, i.getAmount()) ?
                        NamedTextColor.DARK_AQUA
                        : TextColor.color(0, 117, 117);

                text.append(Component.newline())
                        .append(Component.text("- "))
                        .append(Text.itemAndAmount(i).color(color));
            }

            return text.build();
        }
    }
}