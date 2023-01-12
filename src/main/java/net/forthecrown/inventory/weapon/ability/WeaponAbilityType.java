package net.forthecrown.inventory.weapon.ability;

import static net.forthecrown.inventory.weapon.ability.WeaponAbility.START_LEVEL;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.inventory.weapon.WeaponAbilities;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.BaseItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class WeaponAbilityType implements Predicate<User> {
  private final Factory factory;
  private final ImmutableList<ItemStack> recipe;
  private final ItemStack item;
  private final int maxLevel;

  private final Predicate<User> userPredicate;

  private final Component displayName;
  private final ImmutableList<Component> description;

  public WeaponAbilityType(Builder builder) {
    this.factory = Objects.requireNonNull(builder.factory);
    this.recipe = builder.items.build();
    this.item = builder.item;
    this.maxLevel = builder.maxLevel;
    this.userPredicate = Objects.requireNonNull(builder.userPredicate);

    this.displayName = Objects.requireNonNull(builder.displayName);
    this.description = builder.description.build();

    // Check arguments
    Preconditions.checkArgument(
        maxLevel > START_LEVEL,
        "Max level must be above " + START_LEVEL
    );
    Preconditions.checkArgument(
        ItemStacks.notEmpty(item),
        "Cannot have empty item")
    ;
  }

  public static Builder builder(Factory factory) {
    return new Builder(factory);
  }

  public WeaponAbility create() {
    return factory.newAbility(this);
  }

  public WeaponAbility load(CompoundTag tag) {
    var ability = create();
    ability.load(tag);
    return ability;
  }

  public ItemStack getItem() {
    return item.clone();
  }

  public Component fullDisplayName() {
    return displayName
        .hoverEvent(
            TextJoiner.onNewLine()
                .add(description)
                .asComponent()
        );
  }

  public BaseItemBuilder<?> createDisplayItem() {
    var builder = ItemStacks.toBuilder(getItem());
    builder.setName(getDisplayName());
    description.forEach(builder::addLore);

    return builder;
  }

  @Override
  public boolean test(User input) {
    return userPredicate.test(input);
  }

  /* ------------------------- OBJECT OVERRIDES --------------------------- */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WeaponAbilityType that)) {
      return false;
    }

    return getMaxLevel() == that.getMaxLevel()
        && getFactory().equals(that.getFactory())
        && getRecipe().equals(that.getRecipe())
        && getItem().equals(that.getItem());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFactory(), getRecipe(), getItem(), getMaxLevel());
  }

  /* ------------------------------ BUILDER ------------------------------- */

  @Getter
  @Setter
  @Accessors(chain = true, fluent = true)
  @RequiredArgsConstructor
  public static class Builder {
    final Factory factory;
    final ImmutableList.Builder<ItemStack> items = ImmutableList.builder();

    ItemStack item;
    int maxLevel;

    Predicate<User> userPredicate = user -> true;

    Component displayName;
    ImmutableList.Builder<Component> description = ImmutableList.builder();

    public Builder addItem(ItemStack item) {
      Validate.isTrue(ItemStacks.notEmpty(item), "Empty item given");

      items.add(item);
      return this;
    }

    public WeaponAbilityType build() {
      return new WeaponAbilityType(this);
    }

    public WeaponAbilityType registered(String key) {
      var built = build();
      WeaponAbilities.REGISTRY.register(key, built);
      return built;
    }
  }

  @FunctionalInterface
  public interface Factory {
    WeaponAbility newAbility(WeaponAbilityType type);
  }
}