package net.forthecrown.cosmetics;

import com.google.common.collect.ImmutableList;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.user.User;
import net.forthecrown.menu.Slot;
import net.kyori.adventure.text.Component;

public class Cosmetic<T> {

  @Getter
  private final Component displayName;

  @Getter
  private final ImmutableList<Component> description;

  private Component fullDisplayName;

  @Getter
  private final Slot menuSlot;

  @Getter @Setter
  private CosmeticType<T> type;

  @Getter
  private final T value;

  private MenuNode cachedNode;

  public Cosmetic(Builder<T> builder) {
    Objects.requireNonNull(builder.displayName, "Null display name");

    this.displayName = builder.displayName;
    this.description = builder.description.build();

    this.menuSlot = builder.menuSlot;
    Objects.requireNonNull(menuSlot, "No menu slot set");

    this.value = builder.value;
    Objects.requireNonNull(value, "No cosmeticValue set");
  }

  public static <T> Builder<T> builder(T value) {
    return new Builder<>(value);
  }

  public static <T> Cosmetic<T> create(
      T value,
      int slot,
      String displayName,
      String... desc
  ) {
    return builder(value)
        .menuSlot(Slot.of(slot))
        .displayName(displayName)
        .addDescription(desc)
        .build();
  }

  public static <T> Cosmetic<T> create(
      T value,
      int slot,
      Component displayName,
      Component... desc
  ) {
    return builder(value)
        .menuSlot(Slot.of(slot))
        .displayName(displayName)
        .addDescription(desc)
        .build();
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
    return type.getPredicate().test(user, this);
  }

  public MenuNode toMenuNode() {
    if (cachedNode != null) {
      return cachedNode;
    }

    return cachedNode = type.getMenuNodeFactory().createNode(this);
  }

  @Getter @Setter
  @Accessors(fluent = true, chain = true)
  public static class Builder<T> {

    private Component displayName;

    private final ImmutableList.Builder<Component> description = ImmutableList.builder();

    private Slot menuSlot;

    private final T value;

    public Builder(T value) {
      this.value = value;
    }

    public Builder<T> displayName(String name) {
      return displayName(Text.renderString(name));
    }

    public Builder<T> displayName(Component component) {
      this.displayName = component;
      return this;
    }

    public Builder<T> addDescription(String... str) {
      for (var s: str) {
        addDescription(Text.renderString(s));
      }

      return this;
    }

    public Builder<T> addDescription(Component... desc) {
      description.add(desc);
      return this;
    }

    public Builder<T> addDescription(Component desc) {
      description.add(desc);
      return this;
    }

    public Cosmetic<T> build() {
      return new Cosmetic<>(this);
    }
  }
}