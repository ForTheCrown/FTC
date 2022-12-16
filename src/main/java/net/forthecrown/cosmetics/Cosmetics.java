package net.forthecrown.cosmetics;

import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.arrows.ArrowEffects;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.deaths.DeathEffects;
import net.forthecrown.cosmetics.emotes.CosmeticEmote;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;
import net.forthecrown.cosmetics.login.LoginEffect;
import net.forthecrown.cosmetics.login.LoginEffects;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.cosmetics.travel.TravelEffects;
import net.forthecrown.utils.inventory.menu.Menus;

import static net.forthecrown.cosmetics.CosmeticType.initializeValues;

public final class Cosmetics {
    private Cosmetics() {}

    public static final CosmeticType<ArrowEffect>
            ARROWS  = new CosmeticType<>("arrow_effects",  () -> GeneralConfig.effectCost_arrow);

    public static final CosmeticType<DeathEffect>
            DEATH   = new CosmeticType<>("death_effects",  () -> GeneralConfig.effectCost_death);

    public static final CosmeticType<TravelEffect>
            TRAVEL  = new CosmeticType<>("travel_effects", () -> GeneralConfig.effectCost_travel);

    public static final CosmeticType<CosmeticEmote>
            EMOTE   = new CosmeticType<>("emote", null);

    public static final CosmeticType<LoginEffect>
            LOGIN = new CosmeticType<>("login_effects", null);

    @OnEnable
    private static void init() {
        initializeValues(ARROWS, ArrowEffects.class);
        initializeValues(DEATH,  DeathEffects.class);
        initializeValues(TRAVEL, TravelEffects.class);
        initializeValues(EMOTE,  CosmeticEmotes.class);
        initializeValues(LOGIN,  LoginEffects.class);
        Cosmetics.LOGIN.initializeInventory(Menus.DEFAULT_INV_SIZE);

        Registries.COSMETIC.freeze();
    }
}