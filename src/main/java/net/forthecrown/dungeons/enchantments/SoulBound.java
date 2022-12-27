package net.forthecrown.dungeons.enchantments;

import java.util.EnumSet;
import java.util.Set;
import net.forthecrown.core.registry.Keys;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

public class SoulBound extends FtcEnchant {

  public SoulBound() {
    super(
        Keys.forthecrown("soulbound"),
        "Soulbound",
        EnchantmentCategory.VANISHABLE,
        EquipmentSlot.values()
    );
  }

  @Override
  public int getMaxLevel() {
    return 1;
  }

  @Override
  public @NotNull EnchantmentTarget getItemTarget() {
    return EnchantmentTarget.ALL;
  }

  @Override
  public @NotNull Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
    return EnumSet.allOf(org.bukkit.inventory.EquipmentSlot.class);
  }
}