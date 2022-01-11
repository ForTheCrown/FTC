package net.forthecrown.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AfkScanner implements Runnable {
    private BukkitTask task;
    private ScanData lastScan = null;

    public AfkScanner() {
        ComVars.afkScanIntervalTicks.setOnUpdate(l -> schedule());
    }

    void schedule() {
        cancel();

        task = Bukkit.getScheduler().runTaskTimer(Crown.inst(), this, ComVars.afkScanIntervalTicks(), ComVars.afkScanIntervalTicks());
        Crown.logger().info("Scheduled afk scanner");
    }

    void cancel() {
        if(task == null || task.isCancelled()) return;
        task.cancel();
    }

    @Override
    public void run() {
        Crown.logger().info("Running AFK scan");
        ScanData newScan = createScan();

        if(lastScan != null) {
            List<UUID> afk = newScan.getAfk(lastScan);

            for (UUID id: afk) {
                Player p = Bukkit.getPlayer(id);
                if(p == null) continue;

                p.kick(
                        Component.translatable("multiplayer.disconnect.idling"),
                        PlayerKickEvent.Cause.IDLING
                );
            }
        }

        lastScan = newScan;
    }

    public ScanData createScan() {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        ScanData result = new ScanData(online.size());

        for (Player p: online) {
            result.put(p.getUniqueId(), p.getLocation());
        }

        return result;
    }

    public class ScanData extends Object2ObjectOpenHashMap<UUID, Location> {
        public ScanData(int size) {
            super(size);
        }

        public List<UUID> getAfk(ScanData otherScan) {
            List<UUID> afk = new ObjectArrayList<>();

            for (Map.Entry<UUID, Location> e: entrySet()) {
                Location oLoc = otherScan.get(e.getKey());
                if (oLoc == null) continue;
                if(!similarLocation(oLoc, e.getValue())) continue;

                afk.add(e.getKey());
            }

            return afk;
        }
    }

    boolean similarLocation(Location l1, Location l2) {
        return l1.getBlockX() == l2.getBlockX()
                && l1.getBlockY() == l2.getBlockY()
                && l1.getBlockZ() == l2.getBlockZ()
                && Math.floor(l1.getYaw()) == Math.floor(l2.getYaw())
                && Math.floor(l1.getPitch()) == Math.floor(l2.getPitch());
    }
}
