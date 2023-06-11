package net.forthecrown.dungeons.enchantments;

import java.util.Set;
import net.forthecrown.dungeons.DungeonUtils;
import net.minecraft.world.item.enchantment.Enchantments;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SoulBound extends FtcEnchant {

  public SoulBound() {
    super(
        DungeonUtils.royalsKey("soulbound"),
        "Soulbound",
        Enchantments.UNBREAKING.category,
        Enchantments.UNBREAKING.slots
    );
  }

  @Override
  public int getMaxLevel() {
    return 1;
  }

  @Override
  public @NotNull EnchantmentTarget getItemTarget() {
    return Enchantment.DURABILITY.getItemTarget();
  }

  @Override
  public @NotNull Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
    return Enchantment.DURABILITY.getActiveSlots();
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack stack) {
    return Enchantment.DURABILITY.canEnchantItem(stack);
  }
}