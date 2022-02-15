package net.forthecrown.dungeons.boss.components;

import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.SingleEntityBoss;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class TargetUpdateComponent implements BossComponent<SingleEntityBoss> {
    public static final int CHECK_INTERVAL = 40;

    private static final TargetUpdateComponent INSTANCE = new TargetUpdateComponent();
    private TargetUpdateComponent() {}

    public static TargetUpdateComponent getInstance() {
        return INSTANCE;
    }

    private int tick = CHECK_INTERVAL;

    @Override
    public void onSpawn(SingleEntityBoss boss, BossContext context) {
        tick = CHECK_INTERVAL;
    }

    @Override
    public void onTick(SingleEntityBoss boss, BossContext context) {
        tick++;

        if(tick <  CHECK_INTERVAL) return;

        Mob bossEntity = boss.getBossEntity();
        Player target = DungeonUtils.getOptimalTarget(bossEntity, boss.getRoom());

        // Change target only if found target is not already target
        if (target != null && (bossEntity.getTarget() == null || !bossEntity.getTarget().equals(target))) {
            bossEntity.setTarget(target);
        }

        tick = 0;
    }
}
