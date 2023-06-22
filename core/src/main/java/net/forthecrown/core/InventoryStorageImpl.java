package net.forthecrown.core;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.InventoryStorage;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;

/**
 * A class that manages players' inventories to allow for separate inventories
 * between worlds, or between certain programmatically set times
 */
public class InventoryStorageImpl implements InventoryStorage {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final String CATEGORY_SURVIVAL = "survival";

  @Getter
  private static final InventoryStorageImpl storage = new InventoryStorageImpl();

  @Getter
  private final Path path;

  private final Map<UUID, InventoryMap> inventories
      = new Object2ObjectOpenHashMap<>();

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  private InventoryStorageImpl() {
    this.path = PathUtil.pluginPath("stored_inventories.dat");
  }

  /* ------------------------------ METHODS ------------------------------- */

  @Override
  public void clear() {
    inventories.clear();
  }

  @Override
  public void swap(Player player, String category) {
    var current = removeItems(player, category);
    storeInventory(player, category);

    if (current != null) {
      setInventoryContents(player, current);
    }
  }

  @Override
  public void storeInventory(Player player, String category) throws IllegalArgumentException {
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
    player.getInventory().clear();
  }

  @Override
  public boolean hasStoredInventory(Player player, String category) {
    InventoryMap map = inventories.get(player.getUniqueId());

    if (map == null) {
      return false;
    }

    return map.containsKey(category);
  }

  @Override
  public boolean returnItems(Player player, String category) {
    var map = removeItems(player, category);

    if (map == null) {
      return false;
    }

    var inventory = player.getInventory();
    inventory.clear();

    setInventoryContents(player, map);
    map.clear();

    return true;
  }

  @Override
  public boolean giveItems(Player player, String category) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(category);

    if (!hasStoredInventory(player, category)) {
      return false;
    }

    InventoryMap map = inventories.get(player.getUniqueId());

    Int2ObjectMap<ItemStack> items = map.get(category);
    setInventoryContents(player, items);

    return true;
  }

  public Int2ObjectMap<ItemStack> removeItems(Player player, String category) {
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

  private void setInventoryContents(Player player, Int2ObjectMap<ItemStack> map) {
    var inventory = player.getInventory();

    map.forEach((slot, item) -> {
      inventory.setItem(slot, item.clone());
    });
  }

  public Set<String> getExistingCategories(Player player) {
    InventoryMap map = inventories.get(player.getUniqueId());

    if (map == null || map.isEmpty()) {
      return Set.of(CATEGORY_SURVIVAL);
    }

    return map.keySet();
  }


  /* --------------------------- SERIALIZATION ---------------------------- */

  public void save() {
    SerializationHelper.writeTagFile(getPath(), tag -> {
      if (inventories.isEmpty()) {
        return;
      }

      inventories.forEach((uuid, inventoryMap) -> {
        if (inventoryMap.isEmpty()) {
          return;
        }

        CompoundTag invTag = BinaryTags.compoundTag();
        inventoryMap.save(invTag);

        tag.put(uuid.toString(), invTag);
      });

    });
  }

  public void load() {
    clear();

    SerializationHelper.readTagFile(getPath(), tag -> {
      if (tag.isEmpty()) {
        return;
      }

      tag.forEach((s, tag1) -> {
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
        ListTag listTag = BinaryTags.listTag();

        items.forEach((slot, item) -> {
          var itemTag = ItemStacks.save(item);
          itemTag.putInt("slot", slot);

          listTag.add(itemTag);
        });

        tag.put(name, listTag);
      });
    }

    public void load(CompoundTag tag) {
      tag.forEach((name, itemTag) -> {
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