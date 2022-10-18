package net.forthecrown.dungeons.boss.components;

import net.forthecrown.core.Crown;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.SingleEntityBoss;
import net.forthecrown.vars.Var;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class TargetUpdateComponent implements BossComponent<SingleEntityBoss> {
    @Var
    public static int bossTargetUpdateInterval = 40;

    static {
        Crown.getVars().register();
    }

    private TargetUpdateComponent() {}

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