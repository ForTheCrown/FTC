package net.forthecrown.dungeons.boss.components;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.DungeonBoss;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EmptyRoomComponent implements BossComponent {
    public static final int
            emptyRoomCheckInterval = 60 * 20,
            emptyRoomMaxTicks = 30 * 60 * 20;

    private final DungeonBoss boss;
    private int checkTick;
    private int tick;

    public EmptyRoomComponent(DungeonBoss boss) {
        this.boss = boss;
    }

    public static EmptyRoomComponent create(DungeonBoss boss) {
        return new EmptyRoomComponent(boss);
    }

    @Override
    public void onSpawn(DungeonBoss boss, BossContext context) {
        tick = 0;
        checkTick = 0;
    }

    @Override
    public void onTick(DungeonBoss boss, BossContext context) {
        tick++;

        if (tick < emptyRoomCheckInterval) {
            return;
        }

        tick = 0;

        if (isEmpty(boss.getRoom())) {
            checkTick += emptyRoomCheckInterval;
        }

        if (checkTick >= emptyRoomMaxTicks) {
            boss.kill(true);
        }
    }

    boolean isEmpty(WorldBounds3i box) {
        for (Player p: box.getPlayers()) {
            if (p.getGameMode() != GameMode.SPECTATOR) {
                return false;
            }
        }

        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!boss.getRoom().contains(event.getEntity())) {
            return;
        }

        event.getEntity().sendMessage(
                Messages.emptyBossRoomWarning(emptyRoomMaxTicks)
        );
    }
}