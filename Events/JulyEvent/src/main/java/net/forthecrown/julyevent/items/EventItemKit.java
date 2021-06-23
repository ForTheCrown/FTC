package net.forthecrown.julyevent;

import net.forthecrown.core.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EventItemKit {
    private static final ItemStack TRIDENT = new ItemStackBuilder(Material.TRIDENT)
            .setUnbreakable(true)
            .addEnchant(Enchantment.RIPTIDE, 3)
            .build();

    private static final ItemStack ELYTRA = new ItemStackBuilder(Material.ELYTRA)
            .setUnbreakable(true)
            .build();

    public static ItemStack trident(){
        return TRIDENT.clone();
    }

    public static ItemStack elytra(){
        return ELYTRA.clone();
    }

    public static void give(Player player){
        PlayerInventory inv = player.getInventory();
        inv.setChestplate(elytra());
        inv.setItemInMainHand(trident());
    }
}
