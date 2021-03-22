package net.forthecrown.cosmetics.inventories;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.utils.CrownUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EmoteMenu {

    private Inventory inv;
    private final CrownUser user;

    public EmoteMenu(CrownUser user) {
        CustomInventory cinv = new CustomInventory(36, "Emotes", false, true);
        cinv.setHeadItemSlot(0);
        cinv.setReturnItemSlot(4);

        this.inv = cinv.getInventory();
        this.user = user;
    }


    public Inventory getInv() {
        return makeInventory();
    }


    private Inventory makeInventory() {
        Inventory result = this.inv;
        boolean noEmoter = !user.allowsEmotes();

        ItemStack noEmote;
        if (noEmoter) noEmote = CrownUtils.makeItem(Material.BARRIER, 1, true, ChatColor.GOLD + "Emotes Disabled", ChatColor.GRAY + "Right-click to enable sending and receiving emotes.");
        else noEmote = CrownUtils.makeItem(Material.STRUCTURE_VOID, 1, true, ChatColor.GOLD + "Emotes Enabled", ChatColor.GRAY + "Right-click to disable sending and receiving emotes.");
        noEmote.addUnsafeEnchantment(Enchantment.CHANNELING, 0);

        ItemStack bonk = CrownUtils.makeItem(Material.ORANGE_DYE, 1, true, ChatColor.YELLOW + "/bonk", ChatColor.GRAY + "Bonk.");
        ItemStack mwah = CrownUtils.makeItem(Material.ORANGE_DYE, 1, true, ChatColor.YELLOW + "/mwah", ChatColor.GRAY + "Shower your friends with love.");
        ItemStack poke = CrownUtils.makeItem(Material.ORANGE_DYE, 1, true, ChatColor.YELLOW + "/poke", ChatColor.GRAY + "Poking someone makes them jump back a bit.");

        ItemStack hug = CrownUtils.makeItem(Material.GRAY_DYE, 1 , true, ChatColor.YELLOW + "/hug", ChatColor.GRAY + "Hug people :D");

        ItemStack scare = CrownUtils.makeItem(Material.GRAY_DYE, 1, true, ChatColor.YELLOW + "/scare", ChatColor.GRAY + "Can be earned around Halloween.");
        ItemStack jingle = CrownUtils.makeItem(Material.GRAY_DYE, 1, true, ChatColor.YELLOW + "/jingle", ChatColor.GRAY + "Can be earned around Christmas.");
        if (user.getPlayer().hasPermission("ftc.emotes.scare")) scare.setType(Material.ORANGE_DYE);
        if (user.getPlayer().hasPermission("ftc.emotes.jingle")) jingle.setType(Material.ORANGE_DYE);
        if (user.hasPermission("ftc.emotes.hug")) hug.setType(Material.ORANGE_DYE);

        result.setItem(12, bonk);
        result.setItem(13, mwah);
        result.setItem(14, poke);

        result.setItem(21, scare);
        result.setItem(22, jingle);
        result.setItem(23, hug);

        result.setItem(31, noEmote);

        return result;
    }
}
