package net.forthecrown.cosmetics.effects.arrow.effects;

import net.forthecrown.inventory.CrownItems;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class FireworksSpark extends CosmeticArrowEffect {

    @Override
    public double getParticleSpeed() { return 0.1; }

    @Override
    public Particle getParticle() {
        return Particle.FIREWORKS_SPARK;
    }

    @Override
    public ItemStack getEffectItem(boolean isOwned) {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eFirework",
                "&7Almost as if you're using a crossbow.",
                "" ,
                getPurchaseLine(isOwned));
    }
}
