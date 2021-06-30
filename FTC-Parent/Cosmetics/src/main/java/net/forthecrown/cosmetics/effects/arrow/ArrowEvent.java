package net.forthecrown.cosmetics.effects.arrow;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.effects.Vault;
import net.forthecrown.cosmetics.effects.arrow.effects.CosmeticArrowEffect;
import net.forthecrown.cosmetics.effects.arrow.effects.ArrowNone;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class ArrowEvent implements Listener {

    @EventHandler
    public void onPlayerShootsBow(EntityShootBowEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;

        CrownUser user = UserManager.getUser(event.getEntity().getUniqueId());

        Particle activeArrowParticle = user.getArrowParticle();
        if(activeArrowParticle == null) return;

        addParticleToArrow(event.getProjectile(), getEffectFromParticle(activeArrowParticle));
    }

    private static CosmeticArrowEffect getEffectFromParticle(Particle particle) {
        for(CosmeticArrowEffect effect : Vault.getArrowEffects()) {
            if (effect.getParticle().equals(particle)) return effect;
        }
        return new ArrowNone();
    }

    private static void addParticleToArrow(Entity projectile, CosmeticArrowEffect arrowEffect) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Cosmetics.getPlugin(), () -> {
            projectile.getWorld().spawnParticle(arrowEffect.getParticle(), projectile.getLocation(), 1, 0, 0, 0, arrowEffect.getParticleSpeed());
            if (!(projectile.isOnGround() || projectile.isDead())) addParticleToArrow(projectile, arrowEffect);
        }, 1);
    }


}
