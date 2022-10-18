package net.forthecrown.inventory.weapon.upgrades;

import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public record ReforgeUpgrade(Material type,
                             Component itemName,
                             Component loreDisplay,
                             Component... fluff
) implements WeaponUpgrade {

    public static WeaponUpgrade reforge(Material type, Component itemName, Component display, Component... fluff) {
        return new ReforgeUpgrade(type, itemName, display, Validate.noNullElements(fluff));
    }

    public static WeaponUpgrade reforge(Material type, Component itemName, Component display, String... fluff) {
        Validate.noNullElements(fluff);
        Component[] arr = new Component[fluff.length];

        for (int i = 0; i < fluff.length; i++) {
            arr[i] = Text.renderString(fluff[i]);
        }

        return reforge(type, itemName, display, arr);
    }

    @Override
    public void apply(RoyalSword sword, ItemStack item, ItemMeta meta) {
        item.setType(type);
        meta.displayName(itemName);
    }

    @Override
    public Component[] getFlavorText() {
        return fluff;
    }
}