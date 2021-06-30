package net.forthecrown.cosmetics.effects.arrow.effects;

import net.forthecrown.inventory.CrownItems;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class CampfireCozySmoke extends CosmeticArrowEffect {

    @Override
    public double getParticleSpeed() { return 0.005; }

    @Override
    public Particle getParticle() {
        return Particle.CAMPFIRE_COSY_SMOKE;
    }

    @Override
    public ItemStack getEffectItem(boolean isOwned) {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eSmoke",
                "&7Pretend to be a cannon.",
                "",
                getPurchaseLine(isOwned));
    }
}
