package net.forthecrown.utils.inventory;

import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.AbstractListIterator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Static factory/utility class for anything and all things related to
 * {@link ItemStack}s.
 * <p>
 * The most important function in this class is the {@link #isEmpty(ItemStack)} function,
 * which tests item stacks to see if they're null or empty
 * <p>
 * This class allows access to item NBTs with
 * {@link #setUnhandledTags(ItemMeta, CompoundTag)} and {@link #getUnhandledTags(ItemMeta)}.
 * There are limitations with this however, as the method name states, these methods only
 * get and set the tags that Bukkit doesn't handle, handled tags are anything that's like
 * enchantments, attribute modifiers and display info. The rest is free game for us to mess
 * around with as we deem fit.
 * <p>
 * This class also provides static constructors for {@link BaseItemBuilder} implementations.
 * Item builders are classes which provide an interface for constructing {@link ItemStack}
 * objects bit by bit. There are more specialized builders for specific types like
 * {@link #headBuilder()} for a {@link SkullItemBuilder} and {@link #potionBuilder(Material)}
 * for {@link PotionItemBuilder}. If a specific variation of an item builder doesn't exist,
 * add it!
 *
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

    /* ----------------------------- TAGS ------------------------------ */

    /**
     * Sets the item's unhandled tags
     * @param meta The item to set the tags of
     * @param tag The tags to set
     */
    public static void setUnhandledTags(ItemMeta meta, CompoundTag tag) {
        try {
            Field f = getTagField();
            f.setAccessible(true);

            Map<String, Tag> metaTags = (Map<String, Tag>) f.get(meta);
            metaTags.putAll(tag.tags);

            f.set(meta, metaTags);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Couldn't set internalTag in ItemMeta", e);
        }
    }

    /**
     * Gets an items unhandledTags
     * @param meta The item to get the unhandledTags of
     * @return The item's unhandledTags
     */
    public static @NotNull CompoundTag getUnhandledTags(ItemMeta meta) {
        try {
            // Get the tag field and make
            // sure it's accessible
            Field f = getTagField();
            f.setAccessible(true);

            // Get the unhandledTags
            Map<String, Tag> metaTags = (Map<String, Tag>) f.get(meta);

            // Turn the unhandled tags into a compound tag
            CompoundTag result = new CompoundTag();
            result.tags.putAll(metaTags);

            return result;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Couldn't get internalTag in ItemMeta", e);
        }
    }

    /**
     * Gets a tag element in an item's unhandledTags
     * @param meta The meta to get the element of
     * @param key The element's name
     * @return The element
     */
    public static @NotNull CompoundTag getTagElement(ItemMeta meta, String key) {
        return getUnhandledTags(meta).getCompound(key);
    }

    /**
     * Sets an item's tag element
     * @param meta The meta to set the element in
     * @param key The element's name
     * @param tag The element's value
     */
    public static void setTagElement(ItemMeta meta, String key, Tag tag) {
        CompoundTag internalTag = getUnhandledTags(meta);
        internalTag.put(key, tag);

        setUnhandledTags(meta, internalTag);
    }

    /**
     * Removes a tag with the given name from
     * the given item's meta
     * @param meta The meta to remove the tag from
     * @param key The key of the tag to remove
     */
    public static void removeTagElement(ItemMeta meta, String key) {
        CompoundTag internalTag = getUnhandledTags(meta);
        internalTag.remove(key);

        setUnhandledTags(meta, internalTag);
    }

    /**
     * Tests if the given item meta has a tag element
     * with the name of the given key
     * @param meta The meta to test
     * @param key The key to look for
     * @return True, if the meta has a tag element by the given name
     */
    public static boolean hasTagElement(ItemMeta meta, String key) {
        return getUnhandledTags(meta).contains(key);
    }

    /**
     * Gets the unhandledTags field in the ItemMeta class
     * @return The unhandledTags field in the ItemMeta class
     */
    private static Field getTagField() {
        Class meta = metaClass();
        try {
            return meta.getDeclaredField("unhandledTags");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("couldn't find internalTag field", e);
        }
    }

    /**
     * Gets the CraftMetaItem class
     * @return The craft item meta class
     */
    private static Class metaClass() {
        try {
            return Class.forName(craftBukkitInventoryPackage() + ".CraftMetaItem");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Couldn't find class for item meta??????", e);
        }
    }

    /**
     * Gets the bukkit api implementation inventory
     * package's name
     * @return The craft bukkit inventory package name
     */
    private static String craftBukkitInventoryPackage() {
        return CraftItemStack.class.getPackageName();
    }

    /**
     * Saves an item stack to NBT
     * @param item the item to save
     * @return The saved representation of the object
     */
    public static CompoundTag save(ItemStack item) {
        CompoundTag tag = new CompoundTag();
        return CraftItemStack.asNMSCopy(item).save(tag);
    }

    /**
     * Loads an item stack from the given tag
     * @param tag The tag to load from
     * @return The loaded item stack
     */
    public static ItemStack load(CompoundTag tag) {
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.of(tag));
    }

    /* ----------------------------- UTILITY ------------------------------ */

    /**
     * Tests if the item is 'empty'
     * <p>
     * For an item to be 'empty' it has too meet
     * one of the following requirements:
     * <pre>
     * 1. be null
     * 2. be made of air
     * 3. have a quantity of less than 1
     * </pre>
     * @param stack The item to check
     * @return Whether the item is 'empty'
     */
    public static boolean isEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.getType().isAir() || stack.getAmount() < 1;
    }

    /* ----------------------------- INVENTORY ITERATION ------------------------------ */

    /**
     * Runs a consumer on each non-empty item in the given inventory.
     * What 'non-empty' means is defined by {@link #isEmpty(ItemStack)}
     *
     * @param inventory The inventory to run the loop on
     * @param consumer The consumer to apply to the inventory
     */
    public static void forEachNonEmptyStack(Inventory inventory, Consumer<ItemStack> consumer) {
        NonEmptyItemIterator it = nonEmptyIterator(inventory);

        while (it.hasNext()) {
            consumer.accept(it.next());
        }
    }

    /**
     * Creates an inventory iterator that ignores all 'empty'
     * items. Emptiness is determined by {@link #isEmpty(ItemStack)}
     * @param inventory The inventory to iterate through
     * @return The created iterator
     */
    public static NonEmptyItemIterator nonEmptyIterator(Inventory inventory) {
        return new NonEmptyItemIterator(inventory);
    }

    /* ----------------------------- ITEM BUILDERS ------------------------------ */

    /**
     * Creates a new item builder instance
     * <p>
     * Functionally identical to <code>newBuilder(material, 1)</code>
     * @param material The material the builder will use
     * @return The created builder
     */
    public static DefaultItemBuilder builder(Material material) {
        return builder(material, 1);
    }

    /**
     * Creates a new builder with the given material and item amount
     * @param material The material to use
     * @param amount The item quantity to use
     * @return The created builder
     */
    public static DefaultItemBuilder builder(Material material, int amount) {
        return new DefaultItemBuilder(material, amount);
    }

    /**
     * Creates a new skull item builder
     * <p>
     * Functionally identical to <code>newHeadBuilder(1)</code>
     * @return The created builder
     */
    public static SkullItemBuilder headBuilder() {
        return headBuilder(1);
    }

    /**
     * Creates a new skull item builder with the given item quantity
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
     * @param material The material to use
     * @return The created builder
     */
    public static PotionItemBuilder potionBuilder(Material material) {
        return potionBuilder(material, 1);
    }

    /**
     * Creates a new potion item builder with the given quantity
     * @param material The material to use
     * @param amount The quantity to use
     * @return The created builder
     */
    public static PotionItemBuilder potionBuilder(Material material, int amount) {
        return new PotionItemBuilder(material, amount);
    }

    /* ----------------------------- NON EMPTY ITERATOR ------------------------------ */

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