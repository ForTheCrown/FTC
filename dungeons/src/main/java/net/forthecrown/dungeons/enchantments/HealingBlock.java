package net.forthecrown.dungeons.enchantments;

import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.enchantment.FtcEnchant;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;

public class HealingBlock extends FtcEnchant {

  public HealingBlock() {
    super(
        DungeonUtils.royalsKey("healingblock"),
        "Healing Block",
        EnchantmentTarget.WEAPON,
        EquipmentSlot.OFF_HAND,
        EquipmentSlot.HAND
    );
  }
}