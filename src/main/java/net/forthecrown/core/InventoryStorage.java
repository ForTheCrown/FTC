package net.forthecrown.core;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A class that manages players' inventories to allow for separate inventories
 * between worlds, or between certain programmatically set times
 */
public class InventoryStorage {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private static final InventoryStorage storage = new InventoryStorage();

  @Getter
  private final Path path;

  private final Map<UUID, InventoryMap> inventories
      = new Object2ObjectOpenHashMap<>();

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  private InventoryStorage() {
    this.path = PathUtil.pluginPath("stored_inventories.dat");
  }

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Clears all stored inventories
   */
  public void clear() {
    inventories.clear();
  }

  /**
   * Swaps the player's inventory with the inventory stored in the given
   * category.
   * <p>
   * If the player has no stored inventory, their items are stored and inventory
   * cleared. If they do have a stored inventory, then the stored items are
   * returned to the player after their current inventory is stored.
   *
   * @param player   The player whose inventory to swap
   * @param category The category to swap items in
   */
  public void swap(Player player, String category) {
    var current = removeItems(player, category);
    storeInventory(player, category, true);

    if (current != null) {
      setInventoryContents(player, current);
    }
  }

  /**
   * Stores a player's current inventory
   *
   * @param player     The player whose inventory to save
   * @param category   The category to save the player's current inventory into
   * @param clearAfter True, to clear the inventory after it's been stored
   * @throws IllegalArgumentException If there is already an inventory saved in
   *                                  given category for this player
   */
  public void storeInventory(Player player,
                             String category,
                             boolean clearAfter
  ) throws IllegalArgumentException {
    Objects.requireNonNull(category, "Null name");
    Objects.requireNonNull(player, "Null player");

    Preconditions.checkState(
        !hasStoredInventory(player, category),
        "Inventory named %s already stored for player %s",
        category, player
    );

    InventoryMap map = inventories.computeIfAbsent(
        player.getUniqueId(),
        uuid -> new InventoryMap()
    );

    var inv = player.getInventory();
    var it = ItemStacks.nonEmptyIterator(inv);

    Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();

    while (it.hasNext()) {
      int index = it.nextIndex();
      var i = it.next();

      items.put(index, i.clone());
    }

    map.put(category, items);

    if (clearAfter) {
      player.getInventory().clear();
    }
  }

  /**
   * Tests if the player has a stored inventory in the given category
   *
   * @param player   The player to test
   * @param category The name of the category
   * @return True, if the player has a stored inventory in the given category,
   * false otherwise
   */
  public boolean hasStoredInventory(Player player, String category) {
    InventoryMap map = inventories.get(player.getUniqueId());

    if (map == null) {
      return false;
    }

    return map.containsKey(category);
  }

  /**
   * Returns all the contents of a player's stored inventory to the player
   *
   * @param player       The player to return the items to
   * @param category     The name of the category to get the items from
   * @param clearCurrent True, if the player's current inventory should be
   *                     cleared
   * @return True, if the player had an inventory saved in the given category,
   * false otherwise
   */
  public boolean returnItems(Player player,
                             String category,
                             boolean clearCurrent
  ) {
    var map = removeItems(player, category);

    if (map == null) {
      return false;
    }

    var inventory = player.getInventory();

    if (clearCurrent) {
      inventory.clear();
    }

    setInventoryContents(player, map);
    return true;
  }

  /**
   * Gives the items to the player without removing them from the storage.
   * <p>
   * Different from {@link #returnItems(Player, String, boolean)} because it
   * doesn't remove the items from storage before returning them to the user.
   *
   * @param player   The player to return the items to
   * @param category The category to get the items from
   *
   * @return True, if the player had items to give, false if the player had no
   *         storage entry, or if the player had no items saved in the category
   */
  public boolean giveItems(Player player, String category) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(category);

    if (!hasStoredInventory(player, category)) {
      return false;
    }

    InventoryMap map = inventories.get(player.getUniqueId());

    Int2ObjectMap<ItemStack> items = map.get(category);

    var inventory = player.getInventory();
    inventory.clear();

    items.forEach((slot, item) -> {
      inventory.setItem(slot, item.clone());
    });

    return true;
  }

  public Int2ObjectMap<ItemStack> removeItems(Player player,
                                              String category
  ) {
    Objects.requireNonNull(category, "Null name");
    Objects.requireNonNull(player, "Null player");

    InventoryMap map = inventories.get(player.getUniqueId());

    if (map == null) {
      return null;
    }

    var itemMap = map.remove(category);

    if (map.isEmpty()) {
      inventories.remove(player.getUniqueId());
    }

    return itemMap;
  }

  private void setInventoryContents(Player player,
                                    Int2ObjectMap<ItemStack> map
  ) {
    var inventory = player.getInventory();

    map.forEach((slot, item) -> {
      inventory.setItem(slot, item.clone());
    });
    map.clear();
  }

  public Collection<String> getExistingCategories(Player player) {
    InventoryMap map = inventories.get(player.getUniqueId());

    if (map == null || map.isEmpty()) {
      return ObjectSets.emptySet();
    }

    return map.keySet();
  }


  /* --------------------------- SERIALIZATION ---------------------------- */

  @OnSave
  public void save() {
    SerializationHelper.writeTagFile(getPath(), tag -> {
      if (inventories.isEmpty()) {
        return;
      }

      inventories.forEach((uuid, inventoryMap) -> {
        if (inventoryMap.isEmpty()) {
          return;
        }

        CompoundTag invTag = new CompoundTag();
        inventoryMap.save(invTag);

        tag.put(uuid.toString(), invTag);
      });

    });
  }

  @OnLoad
  public void load() {
    clear();

    SerializationHelper.readTagFile(getPath(), tag -> {
      if (tag.isEmpty()) {
        return;
      }

      tag.tags.forEach((s, tag1) -> {
        UUID uuid = UUID.fromString(s);
        InventoryMap map = new InventoryMap();
        map.load((CompoundTag) tag1);

        inventories.put(uuid, map);
      });
    });
  }

  /* ---------------------------- SUB CLASSES ----------------------------- */

  private static class InventoryMap
      extends Object2ObjectOpenHashMap<String, Int2ObjectMap<ItemStack>> {

    public void save(CompoundTag tag) {
      forEach((name, items) -> {
        ListTag listTag = new ListTag();

        items.forEach((slot, item) -> {
          var itemTag = ItemStacks.save(item);
          itemTag.putInt("slot", slot);

          listTag.add(itemTag);
        });

        tag.put(name, listTag);
      });
    }

    public void load(CompoundTag tag) {
      tag.tags.forEach((name, itemTag) -> {
        ListTag itemList = (ListTag) itemTag;
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();

        itemList.forEach(tag1 -> {
          CompoundTag singleItemTag = (CompoundTag) tag1;
          int slot = singleItemTag.getInt("slot");
          singleItemTag.remove("slot");

          if (items.containsKey(slot)) {
            LOGGER.warn("Found duplicate slot {} in '{}' store", slot, name);
          }

          items.put(slot, ItemStacks.load(singleItemTag));
        });

        put(name, items);
      });
    }
  }
}