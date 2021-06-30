package net.forthecrown.cosmetics.effects.emote.emotes;

import net.forthecrown.core.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Hug extends CosmeticEmoteEffect {

    @Override
    public String getEffectName() {
        return "HUG";
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                ChatColor.YELLOW + "/hug", ChatColor.GRAY + "Hug people :D");
    }

    @Override
    public String getPermission() { return "ftc.emotes.hug"; }
}
