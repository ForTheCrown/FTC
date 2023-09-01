package net.forthecrown.text.placeholder;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public abstract class OptionedPlaceholder implements TextPlaceholder {

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
      return null;
    }

    return render(options, render);
  }

  public abstract @Nullable Component render(ParsedOptions options, PlaceholderContext render);
}
