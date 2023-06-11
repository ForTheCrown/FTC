package net.forthecrown.cosmetics;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.RegistryBound;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.Slot;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;

@Getter @Setter
public abstract class Cosmetic implements RegistryBound<Cosmetic> {

  private final Component displayName;
  private final ImmutableList<Component> description;

  @Setter(AccessLevel.PRIVATE) @Getter(AccessLevel.PRIVATE)
  private Component fullDisplayName;

  private final Slot menuSlot;

  private Holder<Cosmetic> holder;

  private int price;
  private CosmeticType<Cosmetic> type;

  public Cosmetic(AbstractBuilder<?> builder) {
    Objects.requireNonNull(builder.displayName, "Null display name");

    this.displayName = builder.displayName;
    this.description = builder.description.build();

    this.menuSlot = builder.menuSlot;
    Objects.requireNonNull(menuSlot, "No menu slot set");
  }

  public Component displayName() {
    if (fullDisplayName != null) {
      return fullDisplayName;
    }

    Component hoverText = TextJoiner.onNewLine().add(description).asComponent();
    fullDisplayName = displayName.hoverEvent(hoverText);

    return fullDisplayName;
  }

  public boolean test(User user) {
    return true;
  }

  public MenuNode toMenuNode() {
    return null;
  }

  @Getter @Setter
  @Accessors(fluent = true, chain = true)
  public abstract static class AbstractBuilder<T extends Cosmetic> {

    private Component displayName;

    private final ImmutableList.Builder<Component> description = ImmutableList.builder();

    private Slot menuSlot;

    public AbstractBuilder<T> addDescription(Component desc) {
      description.add(desc);
      return this;
    }

    public abstract void load(JsonWrapper json);

    protected void loadGeneric(JsonWrapper json) {
      displayName(json.getComponent("displayName"));
      Objects.requireNonNull(displayName, "No 'displayName' set");

      menuSlot(Slot.load(json.get("slot")));
      Objects.requireNonNull(menuSlot, "No 'slot' set");

      if (json.has("description")) {
        JsonArray arr = json.getArray("description");
        arr.forEach(element -> description.add(JsonUtils.readText(element)));
      }
    }

    public abstract T build();
  }
}