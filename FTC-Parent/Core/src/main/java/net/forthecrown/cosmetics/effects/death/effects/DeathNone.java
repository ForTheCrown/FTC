package net.forthecrown.cosmetics.effects.death.effects;

import net.forthecrown.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DeathNone extends CosmeticDeathEffect {

    @Override
    public int getGemCost() { return 0; }

    @Override
    public String getEffectName() {
        return "none";
    }

    @Override
    public void activateEffect(Location loc) {}

    @Override
    public ItemStack getEffectItem(boolean ignored) {
        return CrownItems.makeItem(Material.BARRIER, 1, true,
                "&eNo effect",
                ChatColor.GRAY + "Click to go back to default dying", ChatColor.GRAY + "without any effects.");
    }

    @Override
    public void setItemOwned(ItemStack item) { }
}