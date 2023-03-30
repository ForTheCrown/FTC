package net.forthecrown.utils.inventory;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.TagTypes;
import net.forthecrown.nbt.paper.ItemNbtProvider;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.nbt.string.Snbt;
import net.forthecrown.nbt.string.TagParseException;
import net.forthecrown.utils.AbstractListIterator;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.util.datafix.DataFixers;
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
 * This class also provides static constructors for {@link BaseItemBuilder} implementations. Item
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
 * @see BaseItemBuilder
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
    }

    if (!tag.contains(DATA_VERSION_TAG)) {
      tag.putInt(DATA_VERSION_TAG, Util.getDataVersion());
    }

    if (tag.contains("tag")) {
      var display = tag.getCompound("tag").get("display");

      if (display != null && display.isCompound()) {
        fixDisplayTags(display.asCompound());
      }
    }

    return PaperNbt.loadItem(tag);
  }

  private static void fixDisplayTags(CompoundTag display) {
    String name = display.getString("Name");

    if (!Strings.isNullOrEmpty(name)) {
      display.putString("Name", fixJsonString(name));
    }

    ListTag list = display.getList("Lore", TagTypes.stringType());

    if (list.isEmpty()) {
      return;
    }

    for (int i = 0; i < list.size(); i++) {
      var n = list.get(i);

      if (!n.isString()) {
        continue;
      }

      String s = n.asString().value();
      s = fixJsonString(s);

      list.set(i, BinaryTags.stringTag(s));
    }

    display.put("Lore", list);
  }

  private static String fixJsonString(String json) {
    Component text = GsonComponentSerializer.gson().deserialize(json);
    String plain = Text.plain(text);

    try {
      JsonElement element = JsonParser.parseString(plain);
      Objects.requireNonNull(element);

      return plain;
    } catch (JsonSyntaxException exc) {
      return json;
    }
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

  /* ------------------------ INVENTORY ITERATION ------------------------- */

  /**
   * Runs a consumer on each non-empty item in the given inventory. What 'non-empty' means is
   * defined by {@link #isEmpty(ItemStack)}
   *
   * @param inventory The inventory to run the loop on
   * @param consumer  The consumer to apply to the inventory
   */
  public static void forEachNonEmptyStack(Inventory inventory,
                                          Consumer<ItemStack> consumer
  ) {
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

  public static BaseItemBuilder<?> toBuilder(ItemStack stack) {
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