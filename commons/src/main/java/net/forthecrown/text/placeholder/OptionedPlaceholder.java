package net.forthecrown.text.placeholder;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.forthecrown.Loggers;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class OptionedPlaceholder implements TextPlaceholder {

  private static final Logger LOGGER = Loggers.getLogger();

  private final OptionsArgument options;

  public OptionedPlaceholder(OptionsArgument options) {
    Objects.requireNonNull(options);
    this.options = options;
  }

  @Override
  public @Nullable Component render(String match, PlaceholderContext render) {
    StringReader reader = new StringReader(match);
    ParsedOptions options;

    try {
      options = this.options.parse(reader);
      options.checkAccess(Grenadier.createSource(Bukkit.getConsoleSender()));
    } catch (CommandSyntaxException exc) {
      LOGGER.warn("Failed to parse optioned placeholder, error: '{}'", exc.getMessage());
      return null;
    }

    return render(options, render);
  }

  public abstract @Nullable Component render(ParsedOptions options, PlaceholderContext render);
}
