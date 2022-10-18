package net.forthecrown.cosmetics.deaths;

public final class DeathEffects {
    private DeathEffects() {}

    public static final DeathEffect
            SOUL        = new SoulDeathEffect(),
            TOTEM       = new TotemDeathEffect(),
            EXPLOSION   = new ExplosionDeathEffect(),
            ENDER_RING  = new EnderRingDeathEffect();
}