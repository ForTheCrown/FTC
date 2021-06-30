package net.forthecrown.cosmetics.effects.death.effects;

import net.forthecrown.inventory.CrownItems;
import net.forthecrown.cosmetics.Cosmetics;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

public class Totem extends CosmeticDeathEffect {
    @Override
    public String getEffectName() {
        return "TOTEM";
    }

    @Override
    public void activateEffect(Location loc) {
        double x = loc.getX();
        double y = loc.getY()+1;
        double z = loc.getZ();
        for (int i = 0; i < 20; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Cosmetics.getPlugin(), () -> {
                for (int i1 = 0; i1 < 2; i1++) {
                    loc.getWorld().spawnParticle(Particle.TOTEM, x, y, z, 5, 0, 0, 0, 0.4);
                }
            }, i*1L);
        }
        loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1, 1);
    }

    @Override
    public ItemStack getEffectItem(boolean isOwned) {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eFaulty Totem",
                ChatColor.GRAY + "The particles are there, but you still die?",
                "",
                getPurchaseLine(isOwned));
    }
}
