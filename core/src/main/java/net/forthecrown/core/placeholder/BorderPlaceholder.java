package net.forthecrown.core.placeholder;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.text.placeholder.ParsedPlaceholder;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

public class BorderPlaceholder implements ParsedPlaceholder {

  @Override
  public @Nullable Component render(StringReader reader, PlaceholderContext context)
      throws CommandSyntaxException
  {
    int chars;

    if (reader.canRead()) {
      chars = Readers.readPositiveInt(reader, 1, Integer.MAX_VALUE);
    } else {
      chars = 5;
    }

    String str = " ".repeat(chars);
    return Component.text(str).decorate(TextDecoration.STRIKETHROUGH);
  }
}
