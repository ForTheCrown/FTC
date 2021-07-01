package net.forthecrown.cosmetics.effects.death;

import net.forthecrown.cosmetics.effects.CosmeticConstants;
import net.forthecrown.cosmetics.effects.death.effects.CosmeticDeathEffect;
import net.forthecrown.cosmetics.effects.death.effects.DeathNone;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;

        CrownUser user = UserManager.getUser(event.getEntity().getUniqueId());

        String activeDeathParticle = user.getDeathParticle();
        if (activeDeathParticle == null || activeDeathParticle.equals("") || activeDeathParticle.contains("none")) return;

        Location loc = event.getEntity().getLocation();
        getEffectFromParticleName(activeDeathParticle).activateEffect(loc);
    }

    private static CosmeticDeathEffect getEffectFromParticleName(String name) {
        for(CosmeticDeathEffect effect : CosmeticConstants.getDeathEffects()) {
            if (effect.getEffectName().equals(name)) return effect;
        }
        return new DeathNone();
    }
}