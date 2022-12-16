package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.List;
import java.util.Random;

/** Container that stores the item data of {@link ItemChallenge} instances */
@Getter
@RequiredArgsConstructor
public class ChallengeItemContainer {
    public static final String
            TAG_ACTIVE = "active",
            TAG_POTENTIALS = "potential",
            TAG_PREVIOUS = "previous";

    /** Get of the challenge this container belongs to */
    private final String challengeKey;

    /** Currently active item */
    @Setter
    private ItemStack active = null;

    /** Previously used items that can no longer be reselected */
    private final List<ItemStack> used = new ObjectArrayList<>();

    /** Potential items that may be selected */
    private final List<ItemStack> potentials = new ObjectArrayList<>();

    /* ------------------------------ METHODS ------------------------------- */

    /** Tests if the container is empty */
    public boolean isEmpty() {
        return potentials.isEmpty() && !hasActive();
    }

    /** Tests if the container has an active item */
    public boolean hasActive() {
        return ItemStacks.notEmpty(active);
    }

    /**
     * Fills the container from the given inventory. This method runs
     * recursively, meaning that any item within the given inventory that is
     * either a shulker or chest, will have its contents added to this container
     * as well.
     *
     * @param inventory The inventory to fill from
     */
    public void fillFrom(Inventory inventory) {
        var it = ItemStacks.nonEmptyIterator(inventory);

        while (it.hasNext()) {
            var next = it.next();
            var meta = next.getItemMeta();

            if (meta instanceof BlockStateMeta state
                    && state.getBlockState() instanceof InventoryHolder holder
            ) {
                fillFrom(holder.getInventory());
                continue;
            }

            potentials.add(next.clone());
        }
    }

    /**
     * Randomly selects a new item to become {@link #active}.
     * <p>
     * This method will make a maximum of <code>512</code> attempts to find a
     * new item, if a found item has been added to the {@link #getUsed()} list,
     * it'll try to find a new item.
     * <p>
     * If {@link #getPotentials()} is empty, then null will be returned, if its
     * size is 1, then the first item will be returned.
     *
     * @param random The random to use
     *
     * @return A random item, or null, if {@link #getPotentials()} is empty
     */
    public ItemStack next(Random random) {
        if (potentials.isEmpty()) {
            return null;
        }

        if (potentials.size() == 1) {
            return potentials.get(0).clone();
        }

        ItemStack stack = null;
        short safeGuard = 512;

        while (stack == null || used.contains(stack)) {
            stack = potentials.get(random.nextInt(potentials.size()));

            if (--safeGuard < 0) {
                break;
            }
        }

        return stack == null ? null : stack.clone();
    }

    public void clear() {
        active = null;
        used.clear();
        potentials.clear();
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    public void save(CompoundTag tag) {
        if (hasActive()) {
            tag.put(TAG_ACTIVE, ItemStacks.save(active));
        }

        if (!used.isEmpty()) {
            tag.put(TAG_PREVIOUS,
                    TagUtil.writeCollection(used, ItemStacks::save)
            );
        }

        if (!potentials.isEmpty()) {
            tag.put(TAG_POTENTIALS,
                    TagUtil.writeCollection(potentials, ItemStacks::save)
            );
        }
    }

    public void load(CompoundTag tag) {
        clear();

        if (tag.contains(TAG_ACTIVE)) {
            active = ItemStacks.load(tag.getCompound(TAG_ACTIVE));
        }

        if (tag.contains(TAG_PREVIOUS)) {
            used.addAll(
                    TagUtil.readCollection(
                            tag.get(TAG_PREVIOUS),
                            tag1 -> ItemStacks.load((CompoundTag) tag1)
                    )
            );
        }

        if (tag.contains(TAG_POTENTIALS)) {
            potentials.addAll(
                    TagUtil.readCollection(
                            tag.get(TAG_POTENTIALS),
                            tag1 -> ItemStacks.load((CompoundTag) tag1)
                    )
            );
        }
    }
}