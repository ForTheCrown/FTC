package net.forthecrown.cosmetics.effects.death.effects;

import net.forthecrown.core.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

public class Soul extends CosmeticDeathEffect {
    @Override
    public String getEffectName() {
        return "SOUL";
    }

    @Override
    public void activateEffect(Location loc) {
        double x = loc.getX();
        double y = loc.getY()+1;
        double z = loc.getZ();
        loc.getWorld().playEffect(loc, Effect.ZOMBIE_INFECT, 1);
        for (int i = 0; i < 50; i++) {
            loc.getWorld().spawnParticle(Particle.SOUL, x, y+(i/50), z, 1, 0.5, 0, 0.5, 0.05);
        }
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eSouls",
                ChatColor.GRAY + "Scary souls escaping from your body.",
                "",
                ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
    }
}
