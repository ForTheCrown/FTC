package net.forthecrown.emperor.events;

import net.forthecrown.emperor.CrownCore;
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
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
        if (itemInMainHand.getType() != Material.FIREWORK_STAR || !itemInMainHand.getItemMeta().getDisplayName().contains(ChatColor.GRAY + "Smoke Bomb")) return;
        if (event.getPlayer().hasCooldown(Material.FIREWORK_STAR)) return;

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getPlayer().getGameMode() != GameMode.SPECTATOR) itemInMainHand.setAmount(itemInMainHand.getAmount()-1);
        Egg egg = event.getPlayer().launchProjectile(Egg.class);
        egg.playEffect(EntityEffect.TOTEM_RESURRECT);
        egg.setCustomName(ChatColor.GRAY + "Boom!");
        eggs.add(egg.getUniqueId());
        eggSmoke();

        event.getPlayer().setCooldown(Material.FIREWORK_STAR, 200);
    }

    private void eggSmoke() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CrownCore.inst(), () -> {
            Set<UUID> toRemove = new HashSet<>();
            for (UUID eggID : eggs) {
                try {
                    Location loc = Bukkit.getEntity(eggID).getLocation();
                    loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc.getX(), loc.getY(), loc.getZ(), 0, 0, 0, 0, 1);
                }
                catch (Exception ignored) {
                    toRemove.add(eggID);
                }
            }
            for (UUID uuid : toRemove) eggs.remove(uuid);
            if (!eggs.isEmpty()) eggSmoke();
        }, 3);
    }

    @EventHandler
    public void smokeBomb(PlayerEggThrowEvent event) {
        if (event.getEgg().getCustomName() != null && event.getEgg().getCustomName().contains(ChatColor.GRAY + "Boom!")) {
            event.setHatching(false);
        }
    }

    @EventHandler
    public void smokeBomb(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Egg) || event.getEntity().getCustomName() == null || !event.getEntity().getCustomName().contains(ChatColor.GRAY + "Boom!")) return;

        eggs.remove(event.getEntity().getUniqueId());
        Location loc = event.getEntity().getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.getX(), loc.getY()+1, loc.getZ(), 10, 1, 1, 1, 0);

        double x = loc.getBlockX();
        double y = loc.getBlockY();
        double z = loc.getBlockZ();
        for (int i = -1; i <= 1; i++) {
            for (int j = 0; j <= 2; j++) {
                for (int k = -1; k <= 1; k++) {
                    loc.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, x+i, y+j, z+k, 50, 0.5, 0.5, 0.5, 0.01);
                }
            }
        }

    }
}
