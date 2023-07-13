package net.forthecrown.cosmetics;

import net.forthecrown.cosmetics.emotes.Emotes;
import net.forthecrown.cosmetics.menu.CosmeticMenus;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.cosmetics.travel.TravelEffects;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.registry.RegistryListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class Cosmetics {
  private Cosmetics() {}

  public static final Registry<CosmeticType> TYPES = Registries.newFreezable();

  public static final CosmeticType<Particle> ARROW_EFFECTS
      = CosmeticType.<Particle>builder()
      .defaultNodeFactory("arrow_effects")
      .displayName(Component.text("Arrow Effects"))
      .build();

  public static final CosmeticType<DeathEffect> DEATH_EFFECTS
      = CosmeticType.<DeathEffect>builder()
      .defaultNodeFactory("death_effects")
      .displayName(Component.text("Death Effects"))
      .build();

  public static final CosmeticType<TravelEffect> TRAVEL_EFFECTS
      = CosmeticType.<TravelEffect>builder()
      .defaultNodeFactory("travel_effects")
      .displayName(Component.text("Travel Effects"))
      .build();

  static {
    TYPES.setListener(new RegistryListener<>() {
      @Override
      public void onRegister(Holder<CosmeticType> value) {
        value.getValue().id = value.getId();
        value.getValue().name = value.getKey();
      }

      @Override
      public void onUnregister(Holder<CosmeticType> value) {
        value.getValue().id = -1;
        value.getValue().name = null;
      }
    });
  }

  static void init() {
    // Register Types
    TYPES.register("arrow_effects",  ARROW_EFFECTS);
    TYPES.register("death_effects",  DEATH_EFFECTS);
    TYPES.register("travel_effects", TRAVEL_EFFECTS);
    TYPES.register("emotes",         Emotes.TYPE);
    TYPES.register("login_effects",  LoginEffects.TYPE);

    // Register cosmetics
    ArrowCosmetics.registerAll(ARROW_EFFECTS.getCosmetics());
    DeathCosmetics.registerAll(DEATH_EFFECTS.getCosmetics());
    TravelEffects.registerAll(TRAVEL_EFFECTS.getCosmetics());
    Emotes.registerAll(Emotes.TYPE.getCosmetics());
    LoginEffects.registerAll(LoginEffects.TYPE.getCosmetics());

    CosmeticMenus.createMenus();
  }

  public static Material getCosmeticMaterial(boolean owned) {
    return owned ? Material.ORANGE_DYE : Material.GRAY_DYE;
  }
}