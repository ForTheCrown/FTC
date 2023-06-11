package net.forthecrown.cosmetics;

import com.google.gson.JsonElement;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.cosmetics.Cosmetic.AbstractBuilder;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.registry.RegistryBound;
import net.forthecrown.menu.Menu;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.Menus;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;

public class CosmeticType<T extends Cosmetic> implements RegistryBound<CosmeticType<T>> {

  @Getter
  private Menu menu;

  @Getter
  private final Registry<T> cosmetics = Registries.newRegistry();

  @Getter @Setter
  private Holder<CosmeticType<T>> holder;

  private final Supplier<AbstractBuilder<T>> builderFactory;

  @Getter
  private final Component displayName;

  @Getter
  @Accessors(fluent = true)
  private boolean customRequirement;

  public CosmeticType(Component displayName, Supplier<AbstractBuilder<T>> builderFactory) {
    this.displayName = displayName;
    this.builderFactory = builderFactory;
  }

  public void load(JsonWrapper json) {
    cosmetics.clear();

    int price = json.getInt("price");
    json.remove("price");

    this.customRequirement = json.getBool("custom_requirement");
    json.remove("custom_requirement");

    json.entrySet().forEach(entry -> {
      String key = entry.getKey();
      JsonElement element = entry.getValue();

      JsonWrapper obj = JsonWrapper.wrap(element.getAsJsonObject());

      AbstractBuilder<T> builder = builderFactory.get();
      builder.load(obj);

      T cosmetic = builder.build();
      cosmetic.setPrice(price);
      cosmetic.setType((CosmeticType<Cosmetic>) this);

      cosmetics.register(key, cosmetic);
    });

    MenuBuilder builder = Menus.builder(Menus.sizeFromRows(4), getDisplayName()).addBorder();
    cosmetics.forEach(t -> builder.add(t.getMenuSlot(), t.toMenuNode()));

    menu = builder.build();
  }
}