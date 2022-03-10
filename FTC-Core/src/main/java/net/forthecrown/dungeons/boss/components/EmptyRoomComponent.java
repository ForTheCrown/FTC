package net.forthecrown.dungeons.boss.components;

import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.DungeonBoss;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EmptyRoomComponent implements BossComponent {
    public static final Var<Integer>
            EMPTY_ROOM_CHECK_INTERVAL = Var.def("dungeons_emptyRoomCheckInterval", VarTypes.INT, 60 * 20),
            EMPTY_ROOM_MAX_TICKS      = Var.def("dungeons_emptyRoomMaxTicks", VarTypes.INT, 30 * 60 * 20);

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
        if (tick < EMPTY_ROOM_CHECK_INTERVAL.get()) return;

        tick = 0;

        if(isEmpty(boss.getRoom())) {
            checkTick += EMPTY_ROOM_CHECK_INTERVAL.get();
        }

        if(checkTick >= EMPTY_ROOM_MAX_TICKS.get()) {
            boss.kill(true);
        }
    }

    boolean isEmpty(FtcBoundingBox box) {
        for (Player p: box.getPlayers()) {
            if(p.getGameMode() != GameMode.SPECTATOR) return false;
        }

        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!boss.getRoom().contains(event.getEntity())) return;

        event.getEntity().sendMessage(
                Component.translatable("dungeons.emptyRoomWarn", NamedTextColor.YELLOW,
                        new TimePrinter(TimeUtil.ticksToMillis(EMPTY_ROOM_MAX_TICKS.get()))
                )
        );
    }
}
