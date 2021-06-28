package net.forthecrown.july.items;

import net.forthecrown.core.utils.ItemStackBuilder;
import net.forthecrown.july.JulyMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EventItems {
    private static final ItemStack TRIDENT = new ItemStackBuilder(Material.TRIDENT)
            .setUnbreakable(true)
            .addEnchant(Enchantment.RIPTIDE, 3)
            .build();

    private static final NamespacedKey TICKET_KEY = new NamespacedKey(JulyMain.inst, "ticket");
    private static final ItemStack TICKET = new ItemStackBuilder(Material.FLOWER_BANNER_PATTERN, 1)
            .addData(TICKET_KEY, (byte) 1)
            .addEnchant(Enchantment.LUCK, 1)
            .setName(Component.text("Event Ticket").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
            .addLore(Component.text("Luck III").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
            .setFlags(ItemFlag.HIDE_ENCHANTS)
            .build();

    public static ItemStack trident(){
        return TRIDENT.clone();
    }

    public static ItemStack ticket(){
        return TICKET.clone();
    }

    public static void giveStarter(Player player){
        PlayerInventory inv = player.getInventory();
        inv.setItemInMainHand(trident());
    }
}
