package net.forthecrown.dungeons.rewrite_4.component;

public interface BossHealthListener {
    default void onDamage(BossHealth health, BossHealth.Damage damage) {}
    default void onHealthSet(BossHealth health) {}
}
