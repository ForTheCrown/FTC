package net.forthecrown.cosmetics.travel;

import net.forthecrown.core.Crown;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BeamTravelEffect extends TravelEffect {

    // Simple rocket-like particles when going up, nothing more.
    BeamTravelEffect() {
        super("Beam", new InventoryPos(2, 1),
                Component.text("idk what to put "),
                Component.text("here yet.")
        );
    }

    private void playSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 1f, 2f);
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_AMBIENT, SoundCategory.MASTER, 1.25f, 1.8f);
    }

    @Override // Beam at destination pole, is 1 sec later (to give time to load)
    public void onPoleTeleport(CrownUser user, Location from, final Location pole) {
        summonBeam(from, 4096);
        Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
            summonBeam(pole, 4096);
            playSound(pole);
        }, 20L);
    }

    @Override // Summon a beam each tick for 30 ticks
    public void onHulkStart(CrownUser user, Location loc) {
        playSound(loc);
        summonRepeatingBeam(user.getUniqueId(), loc, 30);
    }

    @Override
    public void onHulkTickUp(CrownUser user, Location loc) {}

    private final Set<UUID> downies = new HashSet<>();
    @Override // Summon a beam each tick for 50 ticks (once)
    public void onHulkTickDown(CrownUser user, Location loc) {
        if (!downies.contains(user.getUniqueId())) {
            downies.add(user.getUniqueId());
            summonRepeatingBeam(user.getUniqueId(), loc.clone().add(0, -20, 0), 50);
        }
    }

    @Override
    public void onHulkLand(CrownUser user, Location landing) {
        downies.remove(user.getUniqueId());
    }



    private void summonBeam(Location loc, int particleAmount) {
        loc.getWorld().spawnParticle(Particle.WARPED_SPORE, loc, particleAmount, 0.05, 50, 0.05, 0, null, true);
    }

    private final Map<UUID, Integer> counters = new HashMap<>();
    private void summonRepeatingBeam(UUID id, Location loc, int duration) {
        counters.put(id, 0); // Use uuid to track duration per user.
        new BukkitRunnable() {
            public void run() {
                if (counters.get(id) < duration) {
                    summonBeam(loc, 1024);
                    counters.replace(id, counters.get(id) + 1);
                }
                else {
                    this.cancel();
                    counters.remove(id);
                }
            }
        }.runTaskTimer(Crown.inst(), 0, 1);
    }
}
