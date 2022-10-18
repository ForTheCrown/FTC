package net.forthecrown.cosmetics;

import net.forthecrown.core.Vars;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.arrows.ArrowEffects;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.deaths.DeathEffects;
import net.forthecrown.cosmetics.emotes.CosmeticEmote;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.cosmetics.travel.TravelEffects;

import static net.forthecrown.cosmetics.CosmeticType.initializeValues;

public final class Cosmetics {
    private Cosmetics() {}

    /**
     *
     */
    public static final CosmeticType<ArrowEffect>   ARROWS  = new CosmeticType<>("arrow_effects",  () -> Vars.effectCost_arrow);

    /**
     *
     */
    public static final CosmeticType<DeathEffect>   DEATH   = new CosmeticType<>("death_effects",  () -> Vars.effectCost_death);

    /**
     *
     */
    public static final CosmeticType<TravelEffect>  TRAVEL  = new CosmeticType<>("travel_effects", () -> Vars.effectCost_travel);

    /**
     *
     */
    public static final CosmeticType<CosmeticEmote> EMOTE   = new CosmeticType<>("emote", null);

    private static void init() {
        initializeValues(Cosmetics.ARROWS, ArrowEffects.class);
        initializeValues(Cosmetics.DEATH,  DeathEffects.class);
        initializeValues(Cosmetics.TRAVEL, TravelEffects.class);
        initializeValues(Cosmetics.EMOTE,  CosmeticEmotes.class);

        Registries.COSMETIC.freeze();
    }
}