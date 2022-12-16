package net.forthecrown.cosmetics.travel;


import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BeamTravelEffect extends TravelEffect {

    // Simple rocket-like particles when going up, nothing more.
    BeamTravelEffect() {
        super("Beam", Slot.of(3, 1),
                Component.text("Beam me up, Scotty ")
        );
    }

    private void playSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 1f, 2f);
        loc.getWorld().playSound(loc, Sound.BLOCK_BEACON_AMBIENT, SoundCategory.MASTER, 1.25f, 1.8f);
    }

    @Override // Beam at destination pole, is 1 sec later (to give time to load)
    public void onPoleTeleport(User user, Location from, final Location pole) {
        summonBeam(from, 4096);
        Bukkit.getScheduler().runTaskLater(FTC.getPlugin(), () -> {
            summonBeam(pole, 4096);
            playSound(pole);
        }, 20L);
    }

    @Override // Summon a beam each tick for 30 ticks
    public void onHulkStart(User user, Location loc) {
        playSound(loc);
        summonRepeatingBeam(user.getUniqueId(), loc, 30);
    }

    @Override
    public void onHulkTickUp(User user, Location loc) {}

    private final Set<UUID> downies = new HashSet<>();
    @Override // Summon a beam each tick for 50 ticks (once)
    public void onHulkTickDown(User user, Location loc) {
        if (!downies.contains(user.getUniqueId())) {
            downies.add(user.getUniqueId());
            summonRepeatingBeam(user.getUniqueId(), loc.clone().add(0, -20, 0), 50);
        }
    }

    @Override
    public void onHulkLand(User user, Location landing) {
        downies.remove(user.getUniqueId());
    }



    private void summonBeam(Location loc, int particleAmount) {
        loc.getWorld().spawnParticle(Particle.WARPED_SPORE, loc, particleAmount, 0.05, 50, 0.05, 0, null, true);
    }

    private final Object2IntMap<UUID> counters = new Object2IntOpenHashMap<>();

    private void summonRepeatingBeam(UUID id, Location loc, int duration) {
        counters.put(id, 0); // Use uuid to track duration per user.
        new BukkitRunnable() {
            public void run() {
                int got = counters.getInt(id);
                if (got < duration) {
                    summonBeam(loc, 1024);
                    counters.put(id, got + 1);
                }
                else {
                    cancel();
                    counters.removeInt(id);
                }
            }
        }.runTaskTimer(FTC.getPlugin(), 0, 1);
    }
}