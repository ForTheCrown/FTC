package net.forthecrown.dungeons.enchantments;

import java.util.Set;
import net.forthecrown.dungeons.DungeonUtils;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class PoisonCrit extends FtcEnchant {

  public PoisonCrit() {
    super(
        DungeonUtils.royalsKey("poisoncrit"),
        "Critical Poison",
        EnchantmentCategory.WEAPON,
        net.minecraft.world.entity.EquipmentSlot.OFFHAND,
        net.minecraft.world.entity.EquipmentSlot.MAINHAND
    );
  }

  @Override
  public @NotNull EnchantmentTarget getItemTarget() {
    return EnchantmentTarget.WEAPON;
  }

  @Override
  public @NotNull Set<EquipmentSlot> getActiveSlots() {
    return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
  }

}