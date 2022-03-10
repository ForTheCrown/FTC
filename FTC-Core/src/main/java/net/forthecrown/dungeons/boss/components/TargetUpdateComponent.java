package net.forthecrown.dungeons.boss.components;

import net.forthecrown.vars.Var;
import net.forthecrown.vars.VarRegistry;
import net.forthecrown.vars.types.VarTypes;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.SingleEntityBoss;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class TargetUpdateComponent implements BossComponent<SingleEntityBoss> {
    public static final Var<Integer> CHECK_INTERVAL = VarRegistry.getSafe("bossTargetUpdateInterval", VarTypes.INT, 40);

    private TargetUpdateComponent() {}

    public static TargetUpdateComponent create() {
        return new TargetUpdateComponent();
    }

    private int tick = CHECK_INTERVAL.get();

    @Override
    public void onSpawn(SingleEntityBoss boss, BossContext context) {
        tick = CHECK_INTERVAL.get();
    }

    @Override
    public void onTick(SingleEntityBoss boss, BossContext context) {
        tick++;

        if(tick <  CHECK_INTERVAL.get()) return;

        Mob bossEntity = boss.getBossEntity();
        Player target = DungeonUtils.getOptimalTarget(bossEntity, boss.getRoom());

        // Change target only if found target is not already target
        if (target != null && (bossEntity.getTarget() == null || !bossEntity.getTarget().equals(target))) {
            bossEntity.setTarget(target);
        }

        tick = 0;
    }
}
