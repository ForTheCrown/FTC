package net.forthecrown.cosmetics;

import java.util.Objects;
import java.util.function.IntSupplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.registry.RegistryListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class CosmeticType<T> {

  private final Registry<Cosmetic<T>> cosmetics;

  private final Component displayName;
  private final Material displayMaterial;

  private final CosmeticPredicate<T> predicate;

  private final MenuNodeFactory<T> menuNodeFactory;

  int id = -1;

  public CosmeticType(Builder<T> builder) {
    this.displayName = builder.displayName;
    this.predicate = builder.predicate;
    this.menuNodeFactory = builder.factory;
    this.displayMaterial = builder.displayMaterial;

    Objects.requireNonNull(displayName, "Null display name");
    Objects.requireNonNull(predicate, "Null predicate");
    Objects.requireNonNull(menuNodeFactory, "Null menu node factory");
    Objects.requireNonNull(displayMaterial, "Null display material");

    this.cosmetics = Registries.newFreezable();
    cosmetics.setListener(new RegistryListener<>() {
      @Override
      public void onRegister(Holder<Cosmetic<T>> value) {
        value.getValue().setType(CosmeticType.this);
      }

      @Override
      public void onUnregister(Holder<Cosmetic<T>> value) {
        value.getValue().setType(null);
      }
    });
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  @Getter @Setter
  @Accessors(fluent = true, chain = true)
  public static class Builder<T> {

    private Component displayName;

    private CosmeticPredicate<T> predicate = CosmeticPredicate.defaultPredicate();

    private MenuNodeFactory<T> factory;

    private Material displayMaterial;

    public Builder<T> defaultNodeFactory(String id) {
      return defaultNodeFactory(() -> {
        CosmeticsPlugin plugin = JavaPlugin.getPlugin(CosmeticsPlugin.class);
        Configuration config = plugin.getConfig();
        return config.getInt("prices." + id);
      });
    }

    public Builder<T> defaultNodeFactory(IntSupplier price) {
      return factory(MenuNodeFactory.defaultFactory(price));
    }

    public CosmeticType<T> build() {
      return new CosmeticType<>(this);
    }
  }
}