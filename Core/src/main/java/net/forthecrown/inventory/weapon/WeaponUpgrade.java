package net.forthecrown.inventory.weapon;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.enchantments.Enchantment;
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
     * @param nbt The item's RoyalItem tag
     */
    void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag nbt);
    Component loreDisplay();

    default Component[] loreFluff() {
        return null;
    }

    static WeaponUpgrade reforge(Material type, Enchantment ench, int enchLevel, Component itemName, Component display, Component... fluff) {
        return new ReforgeUpgrade(type, ench, enchLevel, itemName, display, Validate.noNullElements(fluff));
    }

    static WeaponUpgrade reforge(Material type, Component itemName, Component display, String... fluff) {
        return reforge(type, null, 0, itemName, display, fluff);
    }

    static WeaponUpgrade reforge(Material type, Enchantment ench, int enchLevel, Component itemName, Component display, String... fluff) {
        Validate.noNullElements(fluff);
        Component[] arr = new Component[fluff.length];

        for (int i = 0; i < fluff.length; i++) {
            arr[i] = ChatUtils.convertString(fluff[i], true);
        }

        return reforge(type, ench, enchLevel, itemName, display, arr);
    }

    static WeaponUpgrade dragon(int rank) {
        return new DragonUpgrade(rank);
    }

    class ReforgeUpgrade implements WeaponUpgrade {
        private final Material type;
        private final Component itemName;
        private final Component[] fluff;
        private final Component loreDisplay;

        private final Enchantment enchantment;
        private final int enchLevel;

        public ReforgeUpgrade(Material type, Enchantment ench, int enchLevel, Component itemName, Component loreDisplay, Component... fluff) {
            this.type = type;
            this.enchantment = ench;
            this.enchLevel = enchLevel;
            this.itemName = itemName;
            this.fluff = fluff;
            this.loreDisplay = loreDisplay;
        }

        @Override
        public void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag nbt) {
            item.setType(type);
            meta.displayName(itemName);

            if(enchantment != null) meta.addEnchant(enchantment, enchLevel, true);
        }

        @Override
        public Component loreDisplay() {
            return loreDisplay;
        }

        @Override
        public Component[] loreFluff() {
            return fluff;
        }
    }

    class DragonUpgrade implements WeaponUpgrade {
        private final int rank;

        public DragonUpgrade(int rank) {
            this.rank = rank;
        }

        @Override
        public void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag nbt) {

        }

        @Override
        public Component loreDisplay() {
            return null;
        }
    }
}
