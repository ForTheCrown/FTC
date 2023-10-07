package net.forthecrown.sellshop;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.translatable;

import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface SellMessages {

  static Component soldItems(SellResult result, Material material) {
    return soldItems(result.getSold(), result.getEarned(), material);
  }

  static Component soldItemsTotal(int sold, int earned, Material material) {
    return format("Sold a total of &e{0}&r for &6{1, rhines}&r.",
        NamedTextColor.GRAY,

        Text.itemAndAmount(new ItemStack(material), sold),
        earned
    );
  }

  static Component soldItems(int sold, int earned, Material material) {
    return format("Sold &e{0}&r for &6{1, rhines}&r.",
        NamedTextColor.GRAY,

        Text.itemAndAmount(new ItemStack(material), sold),
        earned
    );
  }

  static Component priceDropped(Material material, int before, int after) {
    return format("Your price for &e{0}&r dropped from &6{1, rhines}&r to &e{2, rhines}&r.",
        NamedTextColor.GRAY,
        translatable(material),
        before, after
    );
  }

}
