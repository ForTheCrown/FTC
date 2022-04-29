package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.dungeons.rewrite_4.DungeonBoss;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.VarRegistry;
import net.forthecrown.vars.types.VarTypes;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Mob;

public class TargetUpdater extends BossComponent implements TickComponent {
    public static final Var<Integer> CHECK_INTERVAL = VarRegistry.def("bossTargetUpdateInterval", VarTypes.INT, 40);
    BossEntity entity;

    @Override
    protected void onBossSet(DungeonBoss boss) {
        entity = boss.getComponent(BossEntity.class);
        Validate.notNull(entity, "No entity component in boss: " + boss.getType().key());
    }

    @Override
    public void tick(long bossTick) {
        if(bossTick % CHECK_INTERVAL.get() != 0) return;

        if(entity.getEntity() instanceof Mob entity) {
            entity.setTarget(DungeonUtils.getOptimalTarget(entity, getBoss().getRoom()));
        }
    }
}