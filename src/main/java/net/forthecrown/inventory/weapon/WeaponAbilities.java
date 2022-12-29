package net.forthecrown.inventory.weapon;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.inventory.weapon.ability.EnderPearlAbility;
import net.forthecrown.inventory.weapon.ability.PotionEffectAbility;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class WeaponAbilities {
  public static final Registry<WeaponAbilityType> REGISTRY
      = Registries.newRegistry();

  public static final WeaponAbilityType ENDER_PEARL
      = WeaponAbilityType.builder(EnderPearlAbility::new)
          .addItem(new ItemStack(Material.ENDER_PEARL, 1))
          .registered("ender_pearl");

  public static final WeaponAbilityType REGEN
      = WeaponAbilityType.builder(type -> new PotionEffectAbility(type, PotionEffectType.REGENERATION))
          .addItem(
              ItemStacks.potionBuilder(Material.POTION)
                  .setBaseEffect(new PotionData(PotionType.REGEN, false, false))
                  .build()
          )
          .registered("regen");

  static void init() {
  }
}