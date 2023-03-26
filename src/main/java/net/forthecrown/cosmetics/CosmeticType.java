package net.forthecrown.cosmetics;

import com.google.common.reflect.Reflection;
import java.util.function.IntSupplier;
import lombok.Getter;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.inventory.menu.Menus;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.WordUtils;

@Getter
public class CosmeticType<T extends Cosmetic> {

  /**
   * The type's name, eg: 'arrow_effects'
   */
  private final String name;

  /**
   * Viewer-friendly display name to display to players and staff
   */
  private final Component displayName;

  /**
   * The price of the type's effect instances
   */
  private final IntSupplier price;

  /**
   * Map of {@link Cosmetic#getSerialId()} to {@link Cosmetic} effect instance
   */
  private final Registry<T> effects = Registries.newFreezable();

  /**
   * The type's inventory
   */
  private Menu menu;

  private final int id;

  public CosmeticType(String name, IntSupplier price) {
    this.name = name;
    this.price = price;

    this.displayName = Component.text(
        WordUtils.capitalizeFully(name.replaceAll("_effects", ""))
    );

    var holder = Cosmetics.REGISTRY.register(name, this);
    this.id = holder.getId();
  }

  static void initializeValues(CosmeticType<?> type, Class<?> c) {
    Reflection.initialize(c);
    type.effects.freeze();
    type.initializeInventory(36);
  }

  public void add(T val) {
    effects.register(val.getSerialId(), val);
  }

  void initializeInventory(int invSize) {
    var builder = CosmeticMenus.baseInventory(invSize, displayName, true)
        .add(
            invSize - (Menus.MIN_INV_SIZE / 2) - 1,
            CosmeticMenus.createUnSelectNode(this)
        );

    for (var v : effects) {
      builder.add(v.getSlot(), v.createNode());
    }

    this.menu = builder.build();
  }

  public int getPrice() {
    if (price == null) {
      return -1;
    }

    return price.getAsInt();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CosmeticType<?> type)) {
      return false;
    }

    return getId() == type.getId();
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}