package net.forthecrown.cosmetics.effects.death.effects;

import net.forthecrown.core.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class None extends CosmeticDeathEffect {
    @Override
    public String getEffectName() {
        return "none";
    }

    @Override
    public void activateEffect(Location loc) { return; }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.BARRIER, 1, true,
                "&eNo effect",
                ChatColor.GRAY + "Click to go back to default dying", ChatColor.GRAY + "without any effects.");
    }

    @Override
    public void setItemOwned(ItemStack item) { }
}
