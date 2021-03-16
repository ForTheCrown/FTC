package net.forthecrown.core;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class CrownItems {

    public static final NamespacedKey ITEM_KEY = new NamespacedKey(FtcCore.getInstance(), "crownitem");

    private CrownItems() {}

    public static boolean isCrownItem(@Nullable ItemStack item){
        if(item == null) return false;
        if(!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().hasLore()) return false;

        ItemMeta meta = item.getItemMeta();
        String displayName = ComponentUtils.getString(meta.displayName());
        Material type = item.getType();

        if (type == Material.GOLDEN_HELMET) {
            return displayName.contains("-Crown-") && ComponentUtils.getString(meta.lore().get(0)).contains("Rank ");
        }
        if(type == Material.NETHERITE_SWORD || type == Material.GOLDEN_SWORD){
            return displayName.contains("-Captain's Cutlass") || displayName.contains("-Royal Sword-");
        }

        return false;
    }

    public static ItemStack getCoins(int amount){
        return CrownUtils.makeItem(Material.SUNFLOWER, 1, true, "&eRhines",
                "&6Worth " + amount + " Rhines", "&8Do /deposit to add this to your balance",
                "&8Minted in the year " + CrownUtils.arabicToRoman(CrownUtils.worldTimeToYears(Bukkit.getWorld("world"))) + ".",
                s());
    }

    private static String s(){
        try {
            return "&8During the reign of " + Bukkit.getOfflinePlayer(FtcCore.getKing()).getName() + ".";
        } catch (Exception e){
            return "&8During the Interregnum";
        }
    }

    //TODO getRoyalSword, getCutlass, getVikingAxe

    public static ItemStack getCrown(int level, String owner ){
        String levelS = CrownUtils.arabicToRoman(level);
        ItemStack crown = CrownUtils.makeItem(Material.GOLDEN_HELMET, 1, false, "&6-&e&lCrown&6-",
                "&7Rank " + levelS,
                "&8--------------------------------",
                "&6Only the worthy shall wear this ",
                "&6symbol of strength and power.",
                "&8Crafted for " + owner);

        ItemMeta meta = crown.getItemMeta();
        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
        AttributeModifier attributeModifier;

        meta.setUnbreakable(true);

        int eLevel = 5;

        if(level > 1){
            attributeModifier = new AttributeModifier(UUID.randomUUID(), "generic.movement_speed", 0.25,
                    AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HEAD);

            meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, attributeModifier);
        }
        if(level > 2){
            attributeModifier = new AttributeModifier(UUID.randomUUID(), "generic.max_health", 20,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD);

            meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, attributeModifier);
        }
        if(level > 3) eLevel = 6;

        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, eLevel, true);
        crown.setItemMeta(meta);

        return crown;
    }
}
