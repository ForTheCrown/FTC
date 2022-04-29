package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class EmptyRoomCheck extends BossComponent implements TickComponent, BossStatusListener {
    public static final Var<Integer>
            EMPTY_ROOM_CHECK_INTERVAL = Var.def("dungeons_emptyRoomCheckInterval", VarTypes.INT, 60 * 20),
            EMPTY_ROOM_MAX_TICKS      = Var.def("dungeons_emptyRoomMaxTicks", VarTypes.INT, 30 * 60 * 20);

    private int checkTick;

    @Override
    public void tick(long bossTick) {
        if (bossTick % EMPTY_ROOM_CHECK_INTERVAL.get() != 0) return;

        if(isEmpty(getBoss().getRoom())) {
            checkTick += EMPTY_ROOM_CHECK_INTERVAL.get();
        }

        if(checkTick >= EMPTY_ROOM_MAX_TICKS.get()) {
            getBoss().kill(true);
        }
    }

    boolean isEmpty(WorldBounds3i box) {
        for (Player p: box.getPlayers()) {
            if(p.getGameMode() != GameMode.SPECTATOR) return false;
        }

        return true;
    }

    @Override
    public void onBossDeath(BossContext context, boolean forced) {}

    @Override
    public void onBossSummon(BossContext context) {
        checkTick = 0;
    }
}