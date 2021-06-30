package net.forthecrown.cosmetics.effects.arrow.effects;

import net.forthecrown.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class DamageIndicator extends CosmeticArrowEffect {

    @Override
    public double getParticleSpeed() { return 0; }

    @Override
    public Particle getParticle() {
        return Particle.DAMAGE_INDICATOR;
    }

    @Override
    public ItemStack getEffectItem(boolean isOwned) {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eDark Hearts",
                ChatColor.GRAY + "Not really a love tap anymore...",
                "",
                getPurchaseLine(isOwned));
    }
}
