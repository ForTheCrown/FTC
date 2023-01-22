package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.spongepowered.math.vector.Vector3i;

/**
 * Container that stores the item data of {@link ItemChallenge} instances
 */
@Getter
@RequiredArgsConstructor
public class ChallengeItemContainer {
  private static final Logger LOGGER = Loggers.getLogger();

  public static final String
      TAG_ACTIVE = "active",
      TAG_PREVIOUS = "previous",
      TAG_WORLD = "world",
      TAG_CHESTS = "chests";

  public static final int MAX_SEARCH_ATTEMPTS = 512;

  /**
   * Get of the challenge this container belongs to
   */
  private final String challengeKey;

  /**
   * Currently active item
   */
  @Setter
  private ItemStack active = null;

  /**
   * Previously used items that can no longer be reselected
   */
  private final List<ItemStack> used = new ObjectArrayList<>();

  private Reference<World> worldReference;
  private final List<Vector3i> chests = new ObjectArrayList<>();

  /* ------------------------------ METHODS ------------------------------- */

  public boolean isEmpty() {
    return !hasActive() && (getChestWorld() == null || chests.isEmpty());
  }

  /**
   * Tests if the container has an active item
   */
  public boolean hasActive() {
    return ItemStacks.notEmpty(active);
  }

  public World getChestWorld() {
    return worldReference == null ? null : worldReference.get();
  }

  public void setChestWorld(World world) {
    this.worldReference = world == null ? null : new WeakReference<>(world);
  }

  public List<ItemStack> getPotentials() {
    var world = getChestWorld();

    if (world == null) {
      LOGGER.error("Cannot get items for {}: world not found", challengeKey);
      return List.of();
    }

    List<ItemStack> items = new ObjectArrayList<>();

    for (var p: chests) {
      var block = Vectors.getBlock(p, world);

      if (!(block.getState() instanceof InventoryHolder holder)) {
        LOGGER.warn("Block at {} is not a container, cannot get items", p);
        continue;
      }

      ItemStacks.forEachNonEmptyStack(
          holder.getInventory(),
          items::add
      );
    }

    return items;
  }


  /**
   * Randomly selects a new item to become {@link #active}.
   * <p>
   * This method will make a maximum of <code>512</code> attempts to find a new
   * item, if a found item has been added to the {@link #getUsed()} list, it'll
   * try to find a new item.
   * <p>
   * If {@link #getPotentials()} is empty, then null will be returned, if its
   * size is 1, then the first item will be returned.
   *
   * @param random The random to use
   * @return A random item, or null, if {@link #getPotentials()} is empty, or it
   *         took longer than {@link #MAX_SEARCH_ATTEMPTS} to find a valid item.
   */
  public CompletableFuture<ItemStack> next(Random random) {
    List<ItemStack> potentials = getPotentials();

    return CompletableFuture.supplyAsync(() -> {
      if (potentials.isEmpty()) {
        return null;
      } else if (potentials.size() == 1) {
        // call findRandom on the single item, this method does not test
        // if the returned item is in the used items list or not
        return findRandom(potentials.get(0), random, new MutableInt());
      }

      ItemStack result = null;

      // If this were C, this could be a simple int* but no, object
      // Tracks how many iterations were made to find an item, if this
      // passes the max search attempts constant, this method returns null
      MutableInt loopCounter = new MutableInt();

      while (ItemStacks.isEmpty(result) || used.contains(result)) {
        result = findRandom(
            potentials.get(random.nextInt(potentials.size())),
            random,
            loopCounter
        );

        if (loopCounter.intValue() > MAX_SEARCH_ATTEMPTS) {
          LOGGER.warn(
              "Couldn't find item in {} iterations, returning null",
              MAX_SEARCH_ATTEMPTS
          );

          return null;
        }
      }

      return result;
    });
  }

  /**
   * Recursively searches for an item from the given item. If the
   * <code>item</code> is a container item, it then looks for an item within
   * that container
   */
  private ItemStack findRandom(ItemStack item,
                               Random random,
                               MutableInt loopCounter
  ) {
    var meta = item.getItemMeta();

    if (!(meta instanceof BlockStateMeta stateMeta)) {
      return item;
    }

    if (!(stateMeta.getBlockState() instanceof InventoryHolder holder)) {
      return item;
    }

    ItemStack stack = null;
    var inv = holder.getInventory();

    while (ItemStacks.isEmpty(stack)) {
      stack = inv.getItem(random.nextInt(inv.getSize()));

      if (loopCounter.incrementAndGet() > MAX_SEARCH_ATTEMPTS) {
        return null;
      }
    }

    return findRandom(stack, random, loopCounter);
  }

  public void clear() {
    active = null;
    used.clear();
    chests.clear();
    worldReference = null;
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

    var world = getChestWorld();
    if (world != null) {
      tag.putString(TAG_WORLD, world.getName());
    }

    if (!chests.isEmpty()) {
      tag.put(TAG_CHESTS, TagUtil.writeCollection(chests, Vectors::writeTag));
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

    if (tag.contains(TAG_WORLD)) {
      World w = Bukkit.getWorld(tag.getString(TAG_WORLD));
      setChestWorld(w);
    }

    if (tag.contains(TAG_CHESTS)) {
      chests.addAll(
          TagUtil.readCollection(tag.get(TAG_CHESTS), Vectors::read3i)
      );
    }
  }
}