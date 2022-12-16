package net.forthecrown.events;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.utils.Tasks;
import org.bukkit.*;
import org.bukkit.entity.Egg;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SmokeBomb implements Listener {

    Set<UUID> eggs = new HashSet<>();

    @EventHandler
    public void smokeBomb(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK
        ) {
            return;
        }

        ItemStack item = event.getPlayer()
                .getInventory()
                .getItemInMainHand();

        var player = event.getPlayer();

        if (item.getType() != Material.FIREWORK_STAR
                || !item.getItemMeta().getDisplayName().contains(ChatColor.GRAY + "Smoke Bomb")
        ) {
            return;
        }

        if (player.hasCooldown(Material.FIREWORK_STAR)) {
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE
                && player.getGameMode() != GameMode.SPECTATOR
        ) {
            item.subtract();
        }

        Egg egg = event.getPlayer().launchProjectile(Egg.class);
        egg.playEffect(EntityEffect.TOTEM_RESURRECT);
        egg.setCustomName(ChatColor.GRAY + "Boom!");
        eggs.add(egg.getUniqueId());
        eggSmoke();

        player.setCooldown(Material.FIREWORK_STAR, 200);
    }

    private void eggSmoke() {
        Tasks.runLater(() -> {
            eggs.removeIf(uuid -> {
                var entity = Bukkit.getEntity(uuid);

                if (entity == null || entity.isDead()) {
                    return true;
                }

                var loc = entity.getLocation();

                new ParticleBuilder(Particle.SMOKE_NORMAL)
                        .location(loc)
                        .spawn();

                return false;
            });

            if (!eggs.isEmpty()) {
                eggSmoke();
            }
        }, 3);
    }

    @EventHandler
    public void smokeBomb(PlayerEggThrowEvent event) {
        if (event.getEgg().getCustomName() == null
                || !event.getEgg().getCustomName().contains(ChatColor.GRAY + "Boom!")
        ) {
            return;
        }

        event.setHatching(false);
    }

    @EventHandler
    public void smokeBomb(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Egg)
                || event.getEntity().getCustomName() == null
                || !event.getEntity().getCustomName().contains(ChatColor.GRAY + "Boom!")
        ) {
            return;
        }

        eggs.remove(event.getEntity().getUniqueId());
        Location loc = event.getEntity().getLocation();
        spawnSmoke(loc);
    }

    public static void spawnSmoke(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);

        new ParticleBuilder(Particle.EXPLOSION_LARGE)
                .location(loc.clone().add(0, 1, 0))
                .offset(1, 1, 1)
                .count(10)
                .extra(0D)
                .spawn();

        var smokeParticle = new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
                .offset(0.5D, 0.5D, 0.5D)
                .extra(0.01D)
                .count(50);
        
        for (int i = -1; i <= 1; i++) {
            for (int j = 0; j <= 2; j++) {
                for (int k = -1; k <= 1; k++) {
                    smokeParticle
                            .location(loc.clone().add(i, j, k))
                            .spawn();
                }
            }
        }
    }
}