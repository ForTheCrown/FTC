package net.forthecrown.economy;

import net.forthecrown.serializer.CrownSerializer;
import org.bukkit.Material;

/**
 * A map of item prices with the prices stored as shorts
 * <p></p>
 * Implementation: {@link ServerItemPriceMap}
 */
public interface ItemPriceMap extends CrownSerializer {

    /**
     * Gets an items price, defaults to 2 if no price was found
     * @param material The material to get the price of
     * @return The price of the material, defaults to 2 if no price was found
     */
    default short get(Material material) {
        return getOrDefault(material, (short) 2);
    }

    /**
     * Sets the price of a material
     * @param material The material to set the price of
     * @param price The price of the item
     */
    void set(Material material, short price);

    /**
     * Gets the price of the material or the default if none found.
     * @param mat The material to get the price of
     * @param def The default price
     * @return The price of the item, default if none found
     */
    short getOrDefault(Material mat, short def);

    /**
     * Checks if the price map contains the given material
     * @param mat The material to check
     * @return Whether the map contains the material
     */
    boolean contains(Material mat);
}
