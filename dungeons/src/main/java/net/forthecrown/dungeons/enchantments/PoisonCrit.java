package net.forthecrown.dungeons.enchantments;

import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.enchantment.FtcEnchant;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;

public class PoisonCrit extends FtcEnchant {

  public PoisonCrit() {
    super(
        DungeonUtils.royalsKey("poisoncrit"),
        "Critical Poison",
        EnchantmentTarget.WEAPON,
        EquipmentSlot.OFF_HAND,
        EquipmentSlot.HAND
    );
  }

}