package net.forthecrown.inventory.weapon.ability;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.forthecrown.inventory.weapon.WeaponAbilities;
import net.forthecrown.utils.inventory.ItemStacks;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class WeaponAbilityType {
  private final Factory factory;
  private final ImmutableList<ItemStack> recipe;

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

  @Getter
  @Accessors(chain = true, fluent = true)
  @RequiredArgsConstructor
  public static class Builder {
    private final Factory factory;
    ImmutableList.Builder<ItemStack> items = ImmutableList.builder();

    public Builder addItem(ItemStack item) {
      Validate.isTrue(ItemStacks.notEmpty(item), "Empty item given");

      items.add(item);
      return this;
    }

    public WeaponAbilityType build() {
      return new WeaponAbilityType(factory, items.build());
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