package net.forthecrown.cosmetics.inventories.effects.death;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.cosmetics.inventories.DeathParticleMenu;
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
        for(CosmeticDeathEffect effect : DeathParticleMenu.getDeathEffects()) {
            if (effect.getEffectName().equals(name)) return effect;
        }
        return new None();
    }
}
