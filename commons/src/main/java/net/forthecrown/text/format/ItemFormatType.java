package net.forthecrown.text.format;

import static net.forthecrown.text.format.TextFormatTypes.DEFAULT;

import net.forthecrown.text.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemFormatType implements TextFormatType {

  @Override
  public @NotNull Component resolve(Object value, String style, Audience audience) {
    // Make sure we're given an item stack
    // If not, just return a default value
    if (!(value instanceof ItemStack item)) {
      return DEFAULT.resolve(value, style, audience);
    }

    // Format name with or without the item quantity
    // prepended onto it.
    boolean withAmount = style.isBlank() || !style.contains("-!amount");
    return withAmount ? Text.itemAndAmount(item) : Text.itemDisplayName(item);
  }
}