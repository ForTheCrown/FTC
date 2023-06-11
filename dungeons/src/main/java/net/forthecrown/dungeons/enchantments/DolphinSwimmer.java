package net.forthecrown.dungeons.enchantments;

import java.util.Set;
import net.forthecrown.dungeons.DungeonUtils;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DolphinSwimmer extends FtcEnchant {

  public DolphinSwimmer() {
    super(
        DungeonUtils.royalsKey("dolphinswimmer"),
        "Dolphin Swimmer",
        EnchantmentCategory.TRIDENT,

        net.minecraft.world.entity.EquipmentSlot.OFFHAND,
        net.minecraft.world.entity.EquipmentSlot.MAINHAND
    );
  }

  @Override
  public @NotNull EnchantmentTarget getItemTarget() {
    return EnchantmentTarget.TRIDENT;
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack stack) {
    return stack.getType() == Material.TRIDENT;
  }

  @Override
  public @NotNull Set<EquipmentSlot> getActiveSlots() {
    return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
  }
}