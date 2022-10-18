package net.forthecrown.inventory;

import net.forthecrown.core.registry.FtcKeyed;
import net.forthecrown.utils.inventory.BaseItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.forthecrown.inventory.ExtendedItems.TAG_CONTAINER;
import static net.forthecrown.inventory.ExtendedItems.TAG_TYPE;

public interface ExtendedItemType<T extends ExtendedItem> extends FtcKeyed {
    @NotNull T create(@Nullable UUID owner);

    @NotNull T load(@NotNull CompoundTag item);

    @NotNull BaseItemBuilder createBaseItem();

    default boolean shouldRemainInInventory() {
        return true;
    }

    default @NotNull ItemStack createItem(@Nullable UUID owner) {
        var builder = createBaseItem();
        T created = create(owner);
        var item = builder.build();

        created.update(item);
        return item.clone();
    }

    default @Nullable T get(@NotNull ItemStack itemStack) {
        if (ItemStacks.isEmpty(itemStack)) {
            return null;
        }

        ExtendedItems.fixLegacyIfNeeded(itemStack);
        var meta = itemStack.getItemMeta();

        if (meta == null
                || !ItemStacks.hasTagElement(meta, TAG_CONTAINER)
        ) {
            return null;
        }

        CompoundTag container = ItemStacks.getTagElement(meta, TAG_CONTAINER);

        if (container.isEmpty()
                || !container.getString(TAG_TYPE).equals(getKey())
        ) {
            return null;
        }

        return load(container.getCompound(ExtendedItems.TAG_DATA));
    }

    default void set(@NotNull T t, @NotNull ItemStack itemStack) {
        t.update(itemStack);
    }

}