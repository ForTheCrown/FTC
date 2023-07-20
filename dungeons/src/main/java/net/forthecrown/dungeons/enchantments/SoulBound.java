package net.forthecrown.dungeons.enchantments;

import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.enchantment.FtcEnchant;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SoulBound extends FtcEnchant {

  public SoulBound() {
    super(
        DungeonUtils.royalsKey("soulbound"),
        "Soulbound",
        Enchantment.DURABILITY
    );
  }

  @Override
  public int getMaxLevel() {
    return 1;
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack stack) {
    return Enchantment.DURABILITY.canEnchantItem(stack);
  }
}