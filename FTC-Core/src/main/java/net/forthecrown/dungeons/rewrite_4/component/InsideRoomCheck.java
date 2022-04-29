package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.dungeons.rewrite_4.DungeonBoss;
import org.apache.commons.lang3.Validate;

public class InsideRoomCheck extends BossComponent implements TickComponent {
    private static final int CHECK_INTERVAL = 20;

    BossEntity entity;

    @Override
    protected void onBossSet(DungeonBoss boss) {
        entity = Validate.notNull(
                boss.getComponent(BossEntity.class),
                "No entity component on type: " + boss.getType().key()
        );
    }

    @Override
    public void tick(long bossTick) {
        if(bossTick % CHECK_INTERVAL != 0) return;

        if(!getBoss().getRoom().contains(entity.getEntity())) {
            entity.getEntity().teleport(getBoss().getSpawnLocation());
        }
    }
}