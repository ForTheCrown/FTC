package net.forthecrown.dungeons.enchantments;

import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.enchantment.FtcEnchant;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;

public class StrongAim extends FtcEnchant {

  public StrongAim() {
    super(
        DungeonUtils.royalsKey("strongaim"),
        "Strong Aim",
        EnchantmentTarget.BOW,
        EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND
    );
  }
}