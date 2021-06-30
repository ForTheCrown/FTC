package net.forthecrown.cosmetics.effects.emote.emotes;

import net.forthecrown.core.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Mwah extends CosmeticEmoteEffect {

    @Override
    public String getEffectName() {
        return "MWAH";
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                ChatColor.YELLOW + "/mwah", ChatColor.GRAY + "Shower your friends with love.");
    }

    @Override
    public String getPermission() { return null; }
}
