package net.forthecrown.mayevent;

import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.core.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class StartingKit {
    public static void give(Player player){
        ItemStack grenades = new ItemStack(Material.FIRE_CHARGE, 5);

        NBT nbt = NbtGetter.ofItemTags(grenades);
        nbt.put("grenade", (byte) 1);
        grenades = NbtGetter.applyTags(grenades, nbt);

        PlayerInventory inv = player.getInventory();

        inv.setItem(EquipmentSlot.HEAD, new ItemStackBuilder(Material.IRON_HELMET).addEnchant(Enchantment.MENDING, 1).build());
        inv.setItem(EquipmentSlot.CHEST, new ItemStackBuilder(Material.IRON_CHESTPLATE).setUnbreakable(true).build());
        inv.setItem(EquipmentSlot.LEGS, new ItemStackBuilder(Material.IRON_LEGGINGS).addEnchant(Enchantment.MENDING, 1).build());
        inv.setItem(EquipmentSlot.FEET, new ItemStackBuilder(Material.IRON_BOOTS).addEnchant(Enchantment.MENDING, 1).build());

        inv.setItem(0, new ItemStackBuilder(Material.IRON_SWORD).addEnchant(Enchantment.MENDING, 1).build());
        inv.setItem(1, new ItemStack(Material.GOLDEN_CARROT, 16));
        inv.setItem(2, grenades);
        inv.setItem(3, new ItemStackBuilder(Material.BOW).setUnbreakable(true).build());
        inv.setItem(4, new ItemStack(Material.ARROW, 32));
    }
}
