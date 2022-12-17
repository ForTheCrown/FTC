package net.forthecrown.dungeons.enchantments;

import java.util.Set;
import net.forthecrown.core.registry.Keys;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class HealingBlock extends FtcEnchant {

  public HealingBlock() {
    super(
        Keys.royals("healingblock"),
        "Healing Block",
        EnchantmentCategory.WEAPON,
        net.minecraft.world.entity.EquipmentSlot.OFFHAND,
        net.minecraft.world.entity.EquipmentSlot.MAINHAND
    );
  }

  @Override
  public @NotNull EnchantmentTarget getItemTarget() {
    return EnchantmentTarget.TOOL;
  }

  @Override
  public @NotNull Set<EquipmentSlot> getActiveSlots() {
    return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
  }
}