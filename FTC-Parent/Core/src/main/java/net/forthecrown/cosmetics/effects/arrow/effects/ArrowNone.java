package net.forthecrown.cosmetics.effects.arrow.effects;

import net.forthecrown.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class ArrowNone extends CosmeticArrowEffect {

    @Override
    public int getGemCost() { return 0; }

    @Override
    public double getParticleSpeed() { return 0; }

    @Override
    public String getEffectName() {
        return null;
    }

    @Override
    public Particle getParticle() {
        return null;
    }

    @Override
    public ItemStack getEffectItem(boolean ignored) {
        return CrownItems.makeItem(Material.BARRIER, 1, true,
                ChatColor.GOLD + "No effect",
                ChatColor.GRAY + "Click to go back to default arrows",
                ChatColor.GRAY + "without any effects.");
    }

    @Override
    public void setItemOwned(ItemStack item) { }
}
