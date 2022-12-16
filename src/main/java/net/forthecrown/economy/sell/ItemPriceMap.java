package net.forthecrown.economy.sell;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.forthecrown.utils.ArrayIterator;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A custom implementation of a simple map
 * for storing item price data.
 * <p>
 * This map uses a single {@link ItemSellData} array with
 * the index being the ordinal of the {@link Material} of
 * each data.
 * <p>
 * When price data is added to this map, the data may be
 * placed at 2 indexes, one for the primary material the
 * data represents, and potentially as well at the index
 * of the {@link ItemSellData#getCompactMaterial()} material.
 * This way, material it won't matter if the material that
 * is being looked up is the base or compact material, it
 * returns the same data.
 */
public class ItemPriceMap implements Iterable<ItemSellData> {
    /**
     * The backing array.
     * <p>
     * This will be initialized to 0, but as entries are added,
     * the array is resized to ensure entries can be inserted
     * into the array
     */
    private ItemSellData[] map = new ItemSellData[0];

    /**
     * Inserts the given data into this map
     * <p>
     * This will potentially insert the given data into 2 places
     * in this map, first it will be inserted at the index
     * corresponding to the data's primary {@link ItemSellData#getMaterial()}
     * and, if it has one, it will then also insert it at the index
     * corresponding to the data's {@link ItemSellData#getCompactMaterial()}
     *
     * @param data The data to add
     */
    public void add(ItemSellData data) {
        insert(data, data.getMaterial().ordinal());

        if (data.canBeCompacted()) {
            insert(data, data.getCompactMaterial().ordinal());
        }
    }

    /**
     * Inserts the given data into this
     * map at the given index.
     * <p>
     * This will ensure the map has the capacity to
     * store this item, if it doesnt, the backing
     * array is resized.
     *
     * @param data The data to insert
     * @param index The index to insert at
     */
    private void insert(ItemSellData data, int index) {
        map = ObjectArrays.ensureCapacity(map, index + 1);
        map[index] = data;
    }

    /**
     * Tests if the given material is contained in this map
     * @param material The material to test for
     * @return True, if this map has a sell data entry for the given material
     */
    public boolean contains(Material material) {
        return get(material) != null;
    }

    /**
     * Gets the item sell data for
     * the corresponding material
     * @param material The material to get the data of
     * @return The material's data, or null, if the material
     *         has no data in this map
     */
    public ItemSellData get(Material material) {
        var index = material.ordinal();

        if (index >= map.length) {
            return null;
        }

        return map[index];
    }

    /**
     * Adds all the entries from the given map
     * into this map
     * @param other The map to add from
     */
    public void addAll(ItemPriceMap other) {
        this.map = ObjectArrays.ensureCapacity(this.map, other.map.length);

        for (var d: other) {
            add(d);
        }
    }

    /**
     * Clears this map by filling
     * the backing array with null values
     */
    public void clear() {
        Arrays.fill(map, null);
    }

    /**
     * Creates an unmodifiable iterator
     * which iterates through all non-null
     * entries in the backing array
     * @return The created iterator
     */
    @NotNull
    @Override
    public Iterator<ItemSellData> iterator() {
        return ArrayIterator.unmodifiable(map);
    }
}