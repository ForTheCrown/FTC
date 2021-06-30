package net.forthecrown.cosmetics.effects.arrow.effects;

import net.forthecrown.core.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class Flame extends CosmeticArrowEffect {

    @Override
    public double getParticleSpeed() { return 0; }

    @Override
    public String getEffectName() {
        return "FLAME";
    }

    @Override
    public Particle getParticle() {
        return Particle.FLAME;
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eFlame",
                ChatColor.GRAY + "Works perfectly with flame arrows.",
                "",
                ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
    }
}
