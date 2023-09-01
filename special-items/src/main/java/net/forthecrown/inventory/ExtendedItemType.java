package net.forthecrown.inventory;

import static net.forthecrown.inventory.ExtendedItems.TAG_CONTAINER;
import static net.forthecrown.inventory.ExtendedItems.TAG_TYPE;

import java.util.UUID;
import net.forthecrown.registry.FtcKeyed;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.inventory.ItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExtendedItemType<T extends ExtendedItem> extends FtcKeyed {

  @NotNull T create(@Nullable UUID owner);

  @NotNull T load(@NotNull CompoundTag item);

  @NotNull ItemBuilder<?> createBaseItem();

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

  default @Nullable T get(@Nullable ItemStack itemStack) {
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

  default void rankUp(ItemStack itemStack, T value) {

  }
}