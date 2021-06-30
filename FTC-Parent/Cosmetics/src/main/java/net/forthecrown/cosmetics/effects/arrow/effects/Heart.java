package net.forthecrown.cosmetics.effects.arrow.effects;

import net.forthecrown.core.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class Heart extends CosmeticArrowEffect {

    @Override
    public double getParticleSpeed() { return 0; }

    @Override
    public Particle getParticle() {
        return Particle.HEART;
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eHearts",
                ChatColor.GRAY + "Time to do some matchmaking...",
                "",
                ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
    }
}
