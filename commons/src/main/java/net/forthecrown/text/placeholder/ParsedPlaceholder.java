package net.forthecrown.text.placeholder;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.Loggers;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public interface ParsedPlaceholder extends TextPlaceholder {

  @Override
  default @Nullable Component render(String match, PlaceholderContext render) {
    StringReader reader = new StringReader(match);
    try {
      return render(reader, render);
    } catch (CommandSyntaxException exc) {
      Loggers.getLogger().warn("Failed to parse placeholder, error: {}", exc.getMessage());
      return null;
    }
  }

  @Nullable Component render(StringReader reader, PlaceholderContext context)
      throws CommandSyntaxException;
}
