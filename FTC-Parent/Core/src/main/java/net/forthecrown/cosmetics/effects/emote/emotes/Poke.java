package net.forthecrown.cosmetics.effects.emote.emotes;

import net.forthecrown.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Poke extends CosmeticEmoteEffect {

    @Override
    public String getEffectName() {
        return "POKE";
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                ChatColor.YELLOW + "/poke", ChatColor.GRAY + "Poking someone makes them jump back a bit.");
    }

    @Override
    public String getPermission() { return null; }
}
