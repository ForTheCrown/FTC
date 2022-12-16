package net.forthecrown.dungeons.boss.components;

import net.forthecrown.core.FTC;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.SingleEntityBoss;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class TargetUpdateComponent implements BossComponent<SingleEntityBoss> {
    private TargetUpdateComponent() {}

    public static final int bossTargetUpdateInterval = 40;

    public static TargetUpdateComponent create() {
        return new TargetUpdateComponent();
    }

    private int tick = bossTargetUpdateInterval;

    @Override
    public void onSpawn(SingleEntityBoss boss, BossContext context) {
        tick = bossTargetUpdateInterval;
    }

    @Override
    public void onTick(SingleEntityBoss boss, BossContext context) {
        tick++;

        if (tick < bossTargetUpdateInterval) {
            return;
        }

        Mob bossEntity = boss.getBossEntity();

        if (bossEntity == null) {
            FTC.getLogger().warn("Boss entity is null");
            return;
        }

        Player target = DungeonUtils.getOptimalTarget(bossEntity, boss.getRoom());

        // Change target only if found target is not already target
        if (target != null && (bossEntity.getTarget() == null
                || !bossEntity.getTarget().equals(target))
        ) {
            bossEntity.setTarget(target);
        }

        tick = 0;
    }
}