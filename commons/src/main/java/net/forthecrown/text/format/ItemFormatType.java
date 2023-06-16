package net.forthecrown.text.format;

import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemFormatType implements TextFormatType {

  @Override
  public @NotNull Component resolveArgument(@NotNull Object value, @NotNull String style) {
    // Make sure we're given an item stack
    // If not, just return a default value
    if (!(value instanceof ItemStack item)) {
      return Text.valueOf(value);
    }

    // Format name with or without the item quantity
    // prepended onto it.
    boolean withAmount = style.isBlank() || !style.contains("-!amount");
    return withAmount ? Text.itemAndAmount(item) : Text.itemDisplayName(item);
  }
}