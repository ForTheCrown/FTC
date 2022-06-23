package net.forthecrown.dungeons.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.DungeonLevelImpl;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface SpawnRequirement {
    default boolean test(Player player) {
        if(!canSpawn(player)) {
            player.sendMessage(denyMessage(player));
            return false;
        }

        return true;
    }

    boolean canSpawn(Player player);
    Component denyMessage(@Nullable Player player);

    Tag save();

    Type getType();

    interface Type {
        SpawnRequirement load(Tag tag);
    }

    static Items items(ItemStack... items) {
        return new Items(ListUtils.isNullOrEmpty(items) ? null : Arrays.asList(items));
    }

    static Items items(Collection<ItemStack> itemStacks) {
        return new Items(itemStacks);
    }

    static LevelCleared levelCleared(DungeonLevelImpl level) {
        return new LevelCleared(level.key());
    }

    static LevelCleared levelCleared(Key levelKey) {
        return new LevelCleared(levelKey);
    }

    class LevelCleared implements SpawnRequirement {
        private final Key level;

        public LevelCleared(Key level) {
            this.level = level;
        }

        public Key getLevel() {
            return level;
        }

        @Override
        public boolean canSpawn(Player player) {
            DungeonLevel l = Registries.DUNGEON_LEVELS.get(level);
            if(l == null) return false;

            return l.isClear();
        }

        @Override
        public Component denyMessage(@Nullable Player player) {
            return Component.translatable("dungeons.levelNotClear", NamedTextColor.YELLOW)
                    .append(Component.newline())
                    .append(Component.translatable("dungeons.levelNotClear2", NamedTextColor.GRAY,
                            Component.text("[findspawners]", NamedTextColor.AQUA)
                                    .clickEvent(ClickEvent.runCommand("/findspawners"))
                                    .hoverEvent(Component.text("Click me !"))
                    ));
        }

        @Override
        public Tag save() {
            return StringTag.valueOf(level.asString());
        }

        @Override
        public Type getType() {
            return Bosses.LEVEL_TYPE;
        }
    }

    class Items implements SpawnRequirement {
        private final Collection<ItemStack> items;

        public Items(Collection<ItemStack> items) {
            this.items = items == null ? List.of() : items;
        }

        public Collection<ItemStack> getItems() {
            return items;
        }

        @Override
        public boolean canSpawn(Player player) {
            PlayerInventory inventory = player.getInventory();
            List<ItemStack> items = new ObjectArrayList<>();

            for (ItemStack i: getItems()) {
                if(!inventory.containsAtLeast(i, i.getAmount())) {
                    return false;
                }

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
                    .append(Component.translatable("dungeons.neededItems"));

            for (ItemStack i: getItems()) {
                TextColor color = inventory == null || inventory.containsAtLeast(i, i.getAmount()) ?
                        NamedTextColor.DARK_AQUA : TextColor.color(0, 117, 117);

                text.append(Component.newline())
                        .append(Component.text("- "))
                        .append(FtcFormatter.itemAndAmount(i).color(color));
            }

            return text.build();
        }

        @Override
        public Tag save() {
            ListTag list = new ListTag();

            for (ItemStack i: getItems()) {
                net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(i);
                CompoundTag tag = nms.save(new CompoundTag());
                list.add(tag);
            }

            return list;
        }

        @Override
        public Type getType() {
            return Bosses.ITEM_TYPE;
        }
    }
}
