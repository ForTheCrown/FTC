package net.forthecrown.cosmetics.effects;

import net.forthecrown.cosmetics.effects.death.effects.*;

import java.util.Collection;
import java.util.Set;

public abstract class Vault {

    // Death Effects
    public static final CosmeticDeathEffect soul = new Soul();
    public static final CosmeticDeathEffect totem = new Totem();
    public static final CosmeticDeathEffect explosion = new Explosion();
    public static final CosmeticDeathEffect enderRing = new EnderRing();
    public static final CosmeticDeathEffect none = new None();

    private static final Set<CosmeticDeathEffect> deathEffects = Set.of(soul, totem, explosion, enderRing, none);
    public static Collection<CosmeticDeathEffect> getDeathEffects() { return deathEffects; }

    // Arrow Effects

    // Emotes
}
