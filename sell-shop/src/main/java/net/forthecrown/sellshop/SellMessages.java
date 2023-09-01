package net.forthecrown.sellshop;

import static net.forthecrown.text.Messages.CLICK_ME;
import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;

import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface SellMessages {

  Component SHOP_WEB_MESSAGE = text("Our webstore", NamedTextColor.GRAY)
      .append(newline())
      .append(
          text("forthecrown.buycraft.net", NamedTextColor.AQUA)
              .hoverEvent(CLICK_ME)
              .clickEvent(openUrl("https://forthecrown.buycraft.net/"))
      );

  static Component soldItems(SellResult result, Material material) {
    return soldItems(result.getSold(), result.getEarned(), material);
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
