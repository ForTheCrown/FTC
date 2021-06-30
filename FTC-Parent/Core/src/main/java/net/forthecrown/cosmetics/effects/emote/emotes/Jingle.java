package net.forthecrown.cosmetics.effects.emote.emotes;

import net.forthecrown.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Jingle extends CosmeticEmoteEffect {

    @Override
    public String getEffectName() {
        return "JINGLE";
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                ChatColor.YELLOW + "/jingle", ChatColor.GRAY + "Can be earned around Christmas.");
    }

    @Override
    public String getPermission() { return "ftc.emotes.jingle"; }
}
