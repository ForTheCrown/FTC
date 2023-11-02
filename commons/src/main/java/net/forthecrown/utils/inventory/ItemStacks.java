package net.forthecrown.utils.inventory;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.ItemNbtProvider;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.nbt.string.Snbt;
import net.forthecrown.nbt.string.TagParseException;
import net.forthecrown.utils.AbstractListIterator;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.util.datafix.DataFixers;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Static factory/utility class for anything and all things related to {@link ItemStack}s.
 * <p>
 * The most important function in this class is the {@link #isEmpty(ItemStack)} function, which
 * tests item stacks to see if they're null or empty
 * <p>
 * This class allows access to item NBTs with {@link #setUnhandledTags(ItemMeta, CompoundTag)} and
 * {@link #getUnhandledTags(ItemMeta)}. There are limitations with this however, as the method name
 * states, these methods only get and set the tags that Bukkit doesn't handle, handled tags are
 * anything that's like enchantments, attribute modifiers and display info. The rest is free game
 * for us to mess around with as we deem fit.
 * <p>
 * This class also provides static constructors for {@link ItemBuilder} implementations. Item
 * builders are classes which provide an interface for constructing {@link ItemStack} objects bit by
 * bit. There are more specialized builders for specific types like {@link #headBuilder()} for a
 * {@link SkullItemBuilder} and {@link #potionBuilder(Material)} for {@link PotionItemBuilder}. If a
 * specific variation of an item builder doesn't exist, add it!
 *
 * @see #isEmpty(ItemStack)
 * @see #setUnhandledTags(ItemMeta, CompoundTag)
 * @see #getUnhandledTags(ItemMeta)
 * @see #builder(Material)
 * @see #headBuilder()
 * @see #potionBuilder(Material)
 * @see ItemBuilder
 * @see DefaultItemBuilder
 * @see PotionItemBuilder
 * @see SkullItemBuilder
 */
public final class ItemStacks {
  private ItemStacks() {}

  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * The NBT tag of the item's data version, used for updating item data using Mojang's
   * {@link DataFixers}
   */
  public static final String LEGACY_TAG_DATA_VERSION = "dataVersion";
  public static final String DATA_VERSION_TAG = "DataVersion";

  private static final Hash.Strategy<ItemStack> ITEM_HASH = new Strategy<>() {
    @Override
    public int hashCode(ItemStack o) {
      return Objects.hash(o.getType(), o.getItemMeta());
    }

    @Override
    public boolean equals(ItemStack a, ItemStack b) {
      return a.isSimilar(b);
    }
  };

  /* ------------------------------- TAGS --------------------------------- */

  /**
   * Sets the item's unhandled tags
   *
   * @param meta The item to set the tags of
   * @param tag  The tags to set
   */
  public static void setUnhandledTags(ItemMeta meta, CompoundTag tag) {
    ItemNbtProvider.provider().setUnhandledTags(meta, tag);
  }

  /**
   * Gets an items unhandledTags
   *
   * @param meta The item to get the unhandledTags of
   * @return The item's unhandledTags
   */
  public static @NotNull CompoundTag getUnhandledTags(ItemMeta meta) {
    return ItemNbtProvider.provider().getUnhandledTags(meta);
  }

  /**
   * Gets a tag element in an item's unhandledTags
   *
   * @param meta The meta to get the element of
   * @param key  The element's name
   * @return The element
   */
  public static @NotNull CompoundTag getTagElement(ItemMeta meta, String key) {
    return getUnhandledTags(meta).getCompound(key);
  }

  /**
   * Sets an item's tag element
   *
   * @param meta The meta to set the element in
   * @param key  The element's name
   * @param tag  The element's value
   */
  public static void setTagElement(ItemMeta meta, String key, BinaryTag tag) {
    CompoundTag internalTag = getUnhandledTags(meta);
    internalTag.put(key, tag);

    setUnhandledTags(meta, internalTag);
  }

  /**
   * Removes a tag with the given name from the given item's meta
   *
   * @param meta The meta to remove the tag from
   * @param key  The key of the tag to remove
   */
  public static void removeTagElement(ItemMeta meta, String key) {
    CompoundTag internalTag = getUnhandledTags(meta);
    internalTag.remove(key);

    setUnhandledTags(meta, internalTag);
  }

  /**
   * Tests if the given item meta has a tag element with the name of the given key
   *
   * @param meta The meta to test
   * @param key  The key to look for
   * @return True, if the meta has a tag element by the given name
   */
  public static boolean hasTagElement(ItemMeta meta, String key) {
    return getUnhandledTags(meta).containsKey(key);
  }

  /**
   * Saves an item stack to NBT
   *
   * @param item the item to save
   * @return The saved representation of the object
   */
  public static CompoundTag save(ItemStack item) {
    return PaperNbt.saveItem(item);
  }

  /**
   * Loads an item stack from the given tag
   *
   * @param tag The tag to load from
   * @return The loaded item stack
   */
  public static ItemStack load(CompoundTag tag) {
    if (tag.contains(LEGACY_TAG_DATA_VERSION)) {
      BinaryTag legacyDataVersion = tag.remove(LEGACY_TAG_DATA_VERSION);
      tag.put(DATA_VERSION_TAG, legacyDataVersion);
    } else if (!tag.contains(DATA_VERSION_TAG)) {
      tag.putInt(DATA_VERSION_TAG, VanillaAccess.getDataVersion());
    }

    return PaperNbt.loadItem(tag);
  }

  /**
   * Converts the given item to an NBT string representation of the item.
   * @param item The item to convert to NBT
   *
   * @return The resulting NBT text, will be an empty string if
   * {@link #isEmpty(ItemStack)} for the item returns true
   */
  public static String toNbtString(ItemStack item) {
    if (isEmpty(item)) {
      return "";
    }

    return save(item).toString();
  }

  /**
   * Parses an item from a given NBT string.
   * @param nbt The NBT string to parse
   * @return The parsed item
   * @throws TagParseException If the given string was not valid NBT.
   */
  public static ItemStack fromNbtString(String nbt)
      throws TagParseException
  {
    return load(Snbt.parseCompound(nbt));
  }

  /* ------------------------------ UTILITY ------------------------------- */

  /**
   * Tests if the item is 'empty'
   * <p>
   * For an item to be 'empty' it has too meet one of the following requirements:
   * <pre>
   * 1. be null
   * 2. be made of air
   * 3. have a quantity of less than 1
   * </pre>
   *
   * @param stack The item to check
   * @return Whether the item is 'empty'
   */
  public static boolean isEmpty(@Nullable ItemStack stack) {
    return stack == null || stack.getType().isAir() || stack.getAmount() < 1;
  }

  /**
   * Negates the value returned by {@link #isEmpty(ItemStack)}, Exists to be used as a method
   * reference for null testing item stacks.
   *
   * @param stack The item to test
   * @return True, if the item is not empty, false otherwise
   */
  public static boolean notEmpty(@Nullable ItemStack stack) {
    return !isEmpty(stack);
  }

  /**
   * Tests if an inventory has room for all items in the specified {@code items} list
   * @param inventory Inventory to test against
   * @param items Item to test
   * @return {@code true}, if there is enough room for all items to be placed into the inventory,
   *         {@code false} otherwise
   * @throws NullPointerException If the {@code inventory} or {@code items} are null
   */
  public static boolean hasRoom(Inventory inventory, Collection<ItemStack> items) {
    Objects.requireNonNull(inventory);
    Objects.requireNonNull(items);

    if (items.isEmpty()) {
      return true;
    }

    // Count item quantities
    Object2IntMap<ItemStack> counted = countItems(items);
    ItemStack[] storage = inventory.getStorageContents();

    for (ItemStack item : storage) {
      if (isEmpty(item)) {
        var firstEntry = firstNonEmpty(counted);

        if (firstEntry == null) {
          continue;
        }

        var entryItem = firstEntry.getKey();
        int maxStack = entryItem.getMaxStackSize();

        int remaining = firstEntry.getIntValue();
        firstEntry.setValue(remaining - maxStack);

        continue;
      }

      var remaining = counted.getInt(item);

      if (remaining < 1) {
        continue;
      }

      int maxStack = item.getMaxStackSize();
      int untilMax = maxStack - item.getAmount();

      counted.put(item, remaining - untilMax);
    }

    return counted.values().intStream().allMatch(pair -> pair < 1);
  }

  private static Entry<ItemStack> firstNonEmpty(
      Object2IntMap<ItemStack> map
  ) {
    for (Entry<ItemStack> n : map.object2IntEntrySet()) {
      if (n.getIntValue() < 1) {
        continue;
      }

      return n;
    }

    return null;
  }

  /**
   * Tests if the specified {@code inventory} has room for the specified {@code item} to be added
   * @param inventory Inventory to test against
   * @param item Item to test
   * @return {@code true}, if the {@code item} can be added to the inventory,
   *         {@code false} otherwise
   */
  public static boolean hasRoom(Inventory inventory, ItemStack item) {
    return hasRoom(inventory, item, item.getAmount());
  }

  /**
   * Tests if the specified {@code inventory} has room for the specified {@code item} to be added
   * @param inventory Inventory to test against
   * @param item Item to test
   * @param requiredCount Amount of items
   * @return {@code true}, if the {@code item} can be added to the inventory,
   *         {@code false} otherwise
   */
  public static boolean hasRoom(Inventory inventory, ItemStack item, int requiredCount) {
    if (requiredCount < 1) {
      return true;
    }

    final int maxStack = item.getMaxStackSize();
    int remaining = requiredCount;

    ItemStack[] storage = inventory.getStorageContents();

    for (ItemStack i : storage) {
      if (isEmpty(i)) {
        remaining -= maxStack;
      } else if (!item.isSimilar(i)) {
        continue;
      } else {
        int untilMax = maxStack - i.getAmount();
        remaining -= untilMax;
      }

      if (remaining < 1) {
        return true;
      }
    }

    return false;
  }

  /**
   * Counts the total amount of each item in a specified {@code items} list.
   * @param items Items to count
   * @return Item to total quantity map
   */
  public static Object2IntMap<ItemStack> countItems(Collection<ItemStack> items) {
    Object2IntMap<ItemStack> map = new Object2IntOpenCustomHashMap<>(ITEM_HASH);
    for (ItemStack item : items) {
      map.computeInt(item, (itemStack, integer) -> {
        if (integer == null) {
          return itemStack.getAmount();
        }

        return integer + itemStack.getAmount();
      });
    }
    return map;
  }

  /**
   * Gives or drops each item in the specified {@code items} array. Or drops them at
   * {@link Inventory#getLocation()}, if there's no space for them.
   *
   * @param inventory Inventory to place items into
   * @param items Items to give
   * @see #giveOrDropItem(Inventory, ItemStack)
   */
  public static void giveOrDrop(Inventory inventory, ItemStack... items) {
    if (items.length == 1) {
      giveOrDropItem(inventory, items[0]);
      return;
    }

    giveOrDrop(inventory, List.of(items));
  }

  /**
   * Gives or drops each item in the specified {@code items} list. Or drops them at
   * {@link Inventory#getLocation()}, if there's no space for them.
   *
   * @param inventory Inventory to place items into
   * @param items Items to give
   * @see #giveOrDropItem(Inventory, ItemStack)
   */
  public static void giveOrDrop(Inventory inventory, Collection<ItemStack> items) {
    for (ItemStack item : items) {
      giveOrDropItem(inventory, item);
    }
  }

  /**
   * Deprecated version of the give or drop function, used in some JS scripts
   * @deprecated Use {@link #giveOrDropItem(Inventory, ItemStack)}
   */
  @Deprecated
  public static void giveOrDropItem(Inventory inventory, Location location, ItemStack itemStack) {
    giveOrDrop(inventory, itemStack);
  }

  /**
   * Places an item into the specified {@code inventory}, if there is space, otherwise, drops the
   * item at {@link Inventory#getLocation()}.
   * <p>
   * Any item given to this method will be split with {@link #splitByMax(ItemStack)} to ensure every
   * part of the item is given or dropped.
   *
   * @param inventory Inventory to place item into
   * @param item Item to place into
   */
  public static void giveOrDropItem(Inventory inventory, ItemStack item) {
    if (ItemStacks.isEmpty(item)) {
      throw new IllegalArgumentException("Empty item stack");
    }

    List<ItemStack> split = splitByMax(item);

    for (ItemStack itemStack : split) {
      if (hasRoom(inventory, itemStack)) {
        inventory.addItem(itemStack.clone());
        return;
      }

      Location location = inventory.getLocation();
      Objects.requireNonNull(location, "Inventory has no location");
      location.getWorld().dropItem(location, itemStack.clone());
    }
  }

  /**
   * Delegate for {@link #splitBySize(ItemStack, int)} with the specified {@code itemStack}'s
   * max stack size as the splitting size
   *
   * @param itemStack Item to split
   * @return Split item list
   */
  public static List<ItemStack> splitByMax(ItemStack itemStack) {
    if (isEmpty(itemStack)) {
      return List.of();
    }
    return splitBySize(itemStack, itemStack.getMaxStackSize());
  }

  /**
   * Divides a specified {@code item} into a list by a specified item {@code size}
   * <p>
   * If the specified {@code item} is empty, an empty list is returned. If it's size is less than
   * the specified {@code size}, a singleton list is returned.
   * <p>
   * The item stack is divided into separate items each limited to the specified {@code size}, an
   * extra item may be included if the item's amount is not perfectly divisible by the size, aka, a
   * remainder item.
   *
   * @param item Item to split
   * @param size Size to split to
   *
   * @return Split item list
   */
  public static List<ItemStack> splitBySize(ItemStack item, int size) {
    if (isEmpty(item)) {
      return List.of();
    }
    if (size < 1) {
      throw new IllegalArgumentException("Cannot split an item into sizes less than 1");
    }

    int amount = item.getAmount();

    if (amount <= size) {
      return List.of(item);
    }

    // Get the amount of max-size stacks to give and the last half-sized stack
    int fullStacks = amount / size;
    int remainder  = amount % size;
    int itemCount  = fullStacks + (remainder > 0 ? 1 : 0);

    List<ItemStack> items = new ArrayList<>(itemCount);

    for (int i = 0; i < fullStacks; i++) {
      items.add(withAmount(item, size));
    }

    if (remainder > 0) {
      items.add(withAmount(item, remainder));
    }

    return items;
  }

  private static ItemStack withAmount(ItemStack i, int amount) {
    ItemStack clone = i.clone();
    clone.setAmount(amount);
    return clone;
  }

  /* ------------------------ INVENTORY ITERATION ------------------------- */

  /**
   * Runs a consumer on each non-empty item in the given inventory. What 'non-empty' means is
   * defined by {@link #isEmpty(ItemStack)}
   *
   * @param inventory The inventory to run the loop on
   * @param consumer  The consumer to apply to the inventory
   */
  public static void forEachNonEmptyStack(Inventory inventory, Consumer<ItemStack> consumer) {
    nonEmptyIterator(inventory).forEachRemaining(consumer);
  }

  /**
   * Creates an inventory iterator that ignores all 'empty' items. Emptiness is determined by
   * {@link #isEmpty(ItemStack)}
   *
   * @param inventory The inventory to iterate through
   * @return The created iterator
   */
  public static NonEmptyItemIterator nonEmptyIterator(Inventory inventory) {
    return new NonEmptyItemIterator(inventory);
  }

  /* --------------------------- ITEM BUILDERS ---------------------------- */

  /**
   * Creates a new item builder instance
   * <p>
   * Functionally identical to <code>newBuilder(material, 1)</code>
   *
   * @param material The material the builder will use
   * @return The created builder
   */
  public static DefaultItemBuilder builder(Material material) {
    return builder(material, 1);
  }

  /**
   * Creates a new builder with the given material and item amount
   *
   * @param material The material to use
   * @param amount   The item quantity to use
   * @return The created builder
   */
  public static DefaultItemBuilder builder(Material material, int amount) {
    return new DefaultItemBuilder(material, amount);
  }

  /**
   * Creates a new skull item builder
   * <p>
   * Functionally identical to <code>newHeadBuilder(1)</code>
   *
   * @return The created builder
   */
  public static SkullItemBuilder headBuilder() {
    return headBuilder(1);
  }

  /**
   * Creates a new skull item builder with the given item quantity
   *
   * @param amount The item quantity to use
   * @return The created builder
   */
  public static SkullItemBuilder headBuilder(int amount) {
    return new SkullItemBuilder(amount);
  }

  /**
   * Creates a new potion item builder
   * <p>
   * Functionally identical to <code>newPotionBuilder(material, 1)</code>
   *
   * @param material The material to use
   * @return The created builder
   */
  public static PotionItemBuilder potionBuilder(Material material) {
    return potionBuilder(material, 1);
  }

  /**
   * Creates a new potion item builder with the given quantity
   *
   * @param material The material to use
   * @param amount   The quantity to use
   * @return The created builder
   */
  public static PotionItemBuilder potionBuilder(Material material, int amount) {
    return new PotionItemBuilder(material, amount);
  }

  public static ItemBuilder<?> toBuilder(ItemStack stack) {
    var meta = stack.getItemMeta();

    if (meta instanceof PotionMeta) {
      return new PotionItemBuilder(stack, meta);
    } else if (meta instanceof SkullMeta) {
      return new SkullItemBuilder(stack, meta);
    } else {
      return new DefaultItemBuilder(stack, meta);
    }
  }

  /* ------------------------- NON EMPTY ITERATOR ------------------------- */

  @RequiredArgsConstructor
  public static class NonEmptyItemIterator extends AbstractListIterator<ItemStack> {

    private final Inventory inventory;

    @Override
    protected boolean shouldSkip(@Nullable ItemStack value) {
      return isEmpty(value);
    }

    @Override
    protected void add(int pos, ItemStack val) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected @Nullable ItemStack get(int pos) {
      return inventory.getItem(pos);
    }

    @Override
    protected void set(int pos, @Nullable ItemStack val) {
      inventory.setItem(pos, val);
    }

    @Override
    protected void remove(int pos) {
      set(pos, null);
    }

    @Override
    protected int size() {
      return inventory.getSize();
    }
  }
}