package net.forthecrown.dungeons.enchantments;

import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.enchantment.FtcEnchant;
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
        EnchantmentTarget.TRIDENT,

        EquipmentSlot.OFF_HAND,
        EquipmentSlot.HAND
    );
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack stack) {
    return stack.getType() == Material.TRIDENT;
  }
}