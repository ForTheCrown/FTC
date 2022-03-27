package net.forthecrown.dungeons.rewrite_4.component;

import net.forthecrown.dungeons.boss.BossContext;

public interface BossStatusListener {
    void onBossDeath(BossContext context, boolean forced);
    void onBossSummon(BossContext context);
}
