package net.forthecrown.dungeons;

import net.forthecrown.ItemGraveService.Filter;
import net.forthecrown.dungeons.enchantments.DungeonEnchantments;
import net.forthecrown.dungeons.listeners.EnchantListeners;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FtcEnchantGraveFilter implements Filter {

  @Override
  public boolean shouldRemain(@NotNull ItemStack item, @NotNull Player player) {
    return EnchantListeners.hasEnchant(item, DungeonEnchantments.SOUL_BOUND);
  }
}