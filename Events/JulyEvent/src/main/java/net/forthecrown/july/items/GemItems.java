package net.forthecrown.july.items;

import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.UUID;

public class GemItems {
    public static String KEY = "gem_worth";

    public static ItemStack makeGemItem(int amount, UUID pickupAllowed){
        ItemStack item = new ItemStackBuilder(Material.DIAMOND)
                .addEnchant(Enchantment.LOYALTY, 1)
                .setName(Component.text("Gems").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                .build();

        NBT nbt = NbtHandler.ofItemTags(item);
        nbt.put(KEY, amount);
        nbt.put("pickup_allowed", pickupAllowed);

        return NbtHandler.applyTags(item, nbt);
    }

    public static Item placeGemItem(Location location, int amount, UUID participant){
        ItemStack item = makeGemItem(amount, participant);
        return location.getWorld().dropItem(location, item, i -> {
            i.setPersistent(true);
            i.setOwner(participant);
            i.setThrower(participant);
            i.setWillAge(false);
            i.setCanMobPickup(false);
            i.setCanPlayerPickup(true);
            i.setGravity(false);
            i.setVelocity(new Vector(0, 0, 0));
        });
    }

    public static int getWorth(ItemStack item){
        NBT nbt = NbtHandler.ofItemTags(item);
        return nbt.getInt(KEY);
    }

    public static boolean isGem(ItemStack item){
        if(item == null) return false;
        if(item.getType() != Material.DIAMOND) return false;

        return NbtHandler.ofItemTags(item).has(KEY);
    }

    public static boolean mayPickUp(Item item, Player player){
        NBT nbt = NbtHandler.ofItemTags(item.getItemStack());

        if(!nbt.hasUUID("pickup_allowed")){
            item.remove();
            return false;
        }

        if(!nbt.getUUID("pickup_allowed").equals(player.getUniqueId())){
            item.remove();
            return false;
        }

        return true;
    }
}