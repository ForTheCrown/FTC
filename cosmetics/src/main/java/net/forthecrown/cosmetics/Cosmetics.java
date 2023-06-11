package net.forthecrown.cosmetics;

import java.util.function.Supplier;
import net.forthecrown.cosmetics.Cosmetic.AbstractBuilder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.text.Text;

public final class Cosmetics {
  private Cosmetics() {}

  public static final Registry<CosmeticType> TYPES = Registries.newFreezable();

  public static CosmeticType<ArrowCosmetic> ARROW_EFFECTS
      = create("arrow_effects", "Arrow Effects", ArrowCosmetic::builder);

  private static <T extends Cosmetic> CosmeticType<T> create(
      String key,
      String displayName,
      Supplier<AbstractBuilder<T>> builderFactory
  ) {
    CosmeticType<T> type = new CosmeticType<>(Text.renderString(displayName), builderFactory);
    TYPES.register(key, type);
    return type;
  }
}