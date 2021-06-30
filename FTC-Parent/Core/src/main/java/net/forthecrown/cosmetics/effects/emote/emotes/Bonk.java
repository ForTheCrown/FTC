package net.forthecrown.cosmetics.effects.emote.emotes;

import net.forthecrown.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Bonk extends CosmeticEmoteEffect {

    @Override
    public String getEffectName() {
        return "BONK";
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                ChatColor.YELLOW + "/bonk", ChatColor.GRAY + "Bonk.");
    }

    @Override
    public String getPermission() { return null; }
}
