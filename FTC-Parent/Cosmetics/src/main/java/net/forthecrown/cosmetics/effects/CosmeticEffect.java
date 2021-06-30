package net.forthecrown.cosmetics.effects;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public interface CosmeticEffect {

    boolean isOwnedBy(CrownUser user);
    boolean isCurrentActiveEffect(CrownUser user);

    String getEffectName();

    ItemStack getEffectItem(boolean isOwned);
    default void addGlow(ItemStack item) { item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1); }
    default void setItemOwned(ItemStack item) { item.setType(Material.ORANGE_DYE); }

    int getGemCost();
    default String getPurchaseLine(Boolean isOwned) {
        if (isOwned) return ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + getGemCost() + ChatColor.GRAY + " gems.";
        else return ChatColor.GOLD + "Owned";
    }

    ClickableOption getClickableOption(CrownUser user);
}
