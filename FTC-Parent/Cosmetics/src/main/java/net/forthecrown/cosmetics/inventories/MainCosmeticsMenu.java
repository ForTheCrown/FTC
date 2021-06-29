package net.forthecrown.cosmetics.inventories;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.inventories.effects.death.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MainCosmeticsMenu extends CustomMenu {

    private static final Map<Integer, CosmeticDeathEffect> deathEffectSlots = Map.of(
            10, new Soul(),
            11, new Totem(),
            12, new Explosion(),
            13, new EnderRing(),
            31, new None()
    );

    public static Collection<CosmeticDeathEffect> getDeathEffects() { return deathEffectSlots.values(); }

    public MainCosmeticsMenu(CrownUser user) {
        setUser(user);
        setInv(buildInventory());
    }

    @Override
    TextComponent getInventoryTitle() { return Component.text()
            .append(Component.text("C").decorate(TextDecoration.BOLD))
            .append(Component.text("osmetics"))
            .build(); }

    @Override
    int getSize() { return 54; }


    public ItemStack getReturnItem() {
        return CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back");
    }

    @Override
    Inventory buildInventory() {
        //header: true, returner: false
        Inventory inv = getBaseInventory();

        inv.setItem(20, CrownItems.makeItem(Material.BOW, 1, true, ChatColor.YELLOW + "Arrow Particle Trails", "", ChatColor.GRAY + "Upgrade your arrows with fancy particle", ChatColor.GRAY + "trails as they fly through the air!"));
        inv.setItem(22, CrownItems.makeItem(Material.TOTEM_OF_UNDYING, 1, true, ChatColor.YELLOW + "Emotes", "", ChatColor.GRAY + "Poking, smooching, bonking and more", ChatColor.GRAY + "to interact with your friends."));
        inv.setItem(24, CrownItems.makeItem(Material.SKELETON_SKULL, 1, true, ChatColor.YELLOW + "Death Particles", "", ChatColor.GRAY + "Make your deaths more spectacular by", ChatColor.GRAY + "exploding into pretty particles!"));

        if (user.allowsRidingPlayers()) {
            inv.setItem(40, CrownItems.makeItem(Material.SADDLE, 1, true,ChatColor.YELLOW + "You can ride other players!", "",
                    ChatColor.GRAY + "Right-click someone to jump on top of them.",
                    ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
                    ChatColor.GRAY + "Click to disable this feature."));
        }
        else {
            inv.setItem(40, CrownItems.makeItem(Material.BARRIER, 1, true, ChatColor.YELLOW + "You've disabled riding other players.", "",
                    ChatColor.GRAY + "Right-click someone to jump on top of them.",
                    ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
                    ChatColor.GRAY + "Click to enable this feature."));
        }

        int gems = user.getGems();
        try {
            ItemStack item = inv.getItem(cinv.getHeadItemSlot());
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.set(1, ChatColor.GRAY + "You have " + ChatColor.GOLD + gems + ChatColor.GRAY + " Gems.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(cinv.getHeadItemSlot(), item);
        } catch (Exception ignored) {}

        return inv;
    }



}
