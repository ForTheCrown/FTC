package net.forthecrown.july;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.FireworkMeta;

public class EventUtils {
    public static void clearEffects(LivingEntity player){
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
    }

    public static Firework spawnFirework(Location loc, int power, FireworkEffect... effect){
        return loc.getWorld().spawn(loc, Firework.class, firework -> {
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffects(effect);
            meta.setPower(power);

            firework.setFireworkMeta(meta);
        });
    }
}
