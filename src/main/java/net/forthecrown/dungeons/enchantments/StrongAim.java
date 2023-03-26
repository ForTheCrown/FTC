package net.forthecrown.dungeons.enchantments;

import java.util.Set;
import net.forthecrown.core.Keys;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class StrongAim extends FtcEnchant {

  public StrongAim() {
    super(
        Keys.royals("strongaim"),
        "Strong Aim",
        EnchantmentCategory.BOW,
        net.minecraft.world.entity.EquipmentSlot.MAINHAND,
        net.minecraft.world.entity.EquipmentSlot.OFFHAND
    );
  }

  @Override
  public @NotNull EnchantmentTarget getItemTarget() {
    return EnchantmentTarget.BOW;
  }

  @Override
  public @NotNull Set<EquipmentSlot> getActiveSlots() {
    return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
  }
}