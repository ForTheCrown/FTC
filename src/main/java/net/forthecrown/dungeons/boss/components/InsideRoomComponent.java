package net.forthecrown.dungeons.boss.components;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.SingleEntityBoss;

public class InsideRoomComponent implements BossComponent<SingleEntityBoss> {
    private InsideRoomComponent() {}

    public static InsideRoomComponent create() {
        return new InsideRoomComponent();
    }

    private int tick;
    private static final int CHECK_INTERVAL = 20;

    @Override
    public void onTick(SingleEntityBoss boss, BossContext context) {
        tick++;

        if(tick > CHECK_INTERVAL) {
            tick = 0;

            if(!boss.getRoom().contains(boss.getBossEntity())) {
                boss.getBossEntity().teleport(boss.getSpawn());
            }
        }
    }
}