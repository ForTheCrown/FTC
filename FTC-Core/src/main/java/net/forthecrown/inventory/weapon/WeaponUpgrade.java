package net.forthecrown.inventory.weapon;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

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
    void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag extraData);
    Component loreDisplay();

    default Component[] loreFluff() {
        return null;
    }

    static WeaponUpgrade reforge(Material type, Component itemName, Component display, Component... fluff) {
        return new ReforgeUpgrade(type, itemName, display, Validate.noNullElements(fluff));
    }

    static WeaponUpgrade reforge(Material type, Component itemName, Component display, String... fluff) {
        Validate.noNullElements(fluff);
        Component[] arr = new Component[fluff.length];

        for (int i = 0; i < fluff.length; i++) {
            arr[i] = ChatUtils.convertString(fluff[i], true);
        }

        return reforge(type, itemName, display, arr);
    }

    static WeaponUpgrade endBoss(int rank) {
        return new EndBossUpgrade(rank);
    }

    record EnchantUpgrade(Enchantment ench, int level) implements WeaponUpgrade {
        @Override
        public void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag extraData) {
            meta.addEnchant(ench, level, true);
        }

        @Override
        public Component loreDisplay() {
            return ench.displayName(level);
        }
    }

    record ModifierUpgrade(double speed, double attack) implements WeaponUpgrade {
        @Override
        public void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag extraData) {

            // Thank you Bukkit, for not providing an API for this
            // Good lord, all this for some base attribute values
            // This was a pain to figure out btw
            Item type = Registry.ITEM.get(CraftNamespacedKey.toMinecraft(item.getType().getKey()));
            SwordItem swordItem = (SwordItem) type;

            double speedBase = -2.1D;

            applyModifier(meta, Attribute.GENERIC_ATTACK_DAMAGE, attack + swordItem.getDamage());
            applyModifier(meta, Attribute.GENERIC_ATTACK_SPEED, speedBase + speed);
        }

        void applyModifier(ItemMeta meta, Attribute attribute, double val) {
            meta.removeAttributeModifier(attribute);
            meta.addAttributeModifier(attribute,
                    new AttributeModifier(
                            UUID.randomUUID(),
                            attribute.getKey().value(),
                            val,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlot.HAND
                    )
            );
        }

        @Override
        public Component loreDisplay() {
            return Component.text("Increased attack damage")
                    .append(speed > 0 ? Component.text(" and speed.") : Component.text("."));
        }
    }

    record ReforgeUpgrade(Material type, Component itemName,
                          Component loreDisplay,
                          Component... fluff) implements WeaponUpgrade {

        @Override
        public void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag extraData) {
            item.setType(type);
            meta.displayName(itemName);
        }

        @Override
        public Component[] loreFluff() {
            return fluff;
        }
    }

    record EndBossUpgrade(int rank) implements WeaponUpgrade {
        @Override
        public void apply(RoyalSword sword, ItemStack item, ItemMeta meta, CompoundTag extraData) {

        }

        @Override
        public Component loreDisplay() {
            return null;
        }
    }
}
