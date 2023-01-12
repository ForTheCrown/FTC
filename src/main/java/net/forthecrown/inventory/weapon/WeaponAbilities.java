package net.forthecrown.inventory.weapon;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.inventory.weapon.ability.EnderPearlAbility;
import net.forthecrown.inventory.weapon.ability.PotionEffectAbility;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
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
          .item(new ItemStack(Material.ENDER_PEARL, 1))
          .displayName(Component.text("Ender pearl"))
          .maxLevel(5)
          .addItem(new ItemStack(Material.ENDER_PEARL, 1))
          .registered("ender_pearl");

  public static final WeaponAbilityType REGEN
      = WeaponAbilityType.builder(type -> new PotionEffectAbility(type, PotionEffectType.REGENERATION))
          .maxLevel(5)
          .displayName(Component.text("Regeneration"))

          .item(
              ItemStacks.potionBuilder(Material.POTION)
                  .setBaseEffect(new PotionData(PotionType.REGEN, false, false))
                  .build()
          )

          .addItem(
              ItemStacks.potionBuilder(Material.POTION)
                  .setBaseEffect(new PotionData(PotionType.REGEN, false, false))
                  .build()
          )
          .registered("regen");

  static void init() {
  }
}