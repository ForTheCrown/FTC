package net.forthecrown.inventory.weapon.upgrades;

import net.forthecrown.inventory.weapon.RoyalSword;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * An upgrade is something that is applied to a weapon when it ascends
 * to a certain rank
 */
public interface WeaponUpgrade {
    /**
     * Applies the upgrade to the given item
     * @param item The ItemStack itself
     * @param meta The item's meta
     * @param extraData The item's extra data
     */
    void apply(RoyalSword sword, ItemStack item, ItemMeta meta);

    /**
     * Gets the text to display in the item's lore
     * under the "Next Upgrade: " section
     * @return The upgrade's display text
     */
    Component loreDisplay();

    /**
     * Gets the flavor text this upgrade will display, will remain until
     * a following upgrade replaces the text
     * @return The upgrade's flavor text
     */
    default Component[] getFlavorText() {
        return null;
    }

    /**
     * Gets the display name to display under the
     * "When in main hand" lind in the lore.
     * @return The status display name
     */
    default Component[] statusDisplay() {
        return null;
    }
}