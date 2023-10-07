package net.forthecrown.core.placeholder;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.forthecrown.core.placeholder.ComponentPlaceholder.StringParser;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.ParsedPlaceholder;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.PlaceholderSource;
import net.forthecrown.text.placeholder.TextPlaceholder;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.forthecrown.utils.VanillaAccess;
import net.kyori.adventure.text.Component;

public class TranslatedPlaceholders implements PlaceholderSource {

  @Override
  public TextPlaceholder getPlaceholder(String name, PlaceholderContext ctx) {
    Locale locale;
    User viewer = Audiences.getUser(ctx.viewer());

    if (viewer == null) {
      locale = Locale.ENGLISH;
    } else {
      locale = viewer.getLocale();
    }

    if (!VanillaAccess.isValidTranslationKey(name, locale)) {
      return null;
    }

    return new TranslatedPlaceholder(name, locale);
  }

  public record TranslatedPlaceholder(String key, Locale locale) implements ParsedPlaceholder {

    private static final ArrayArgument<String> argsParser
        = ArgumentTypes.array(new StringParser());

    @Override
    public Component render(StringReader reader, PlaceholderContext context)
        throws CommandSyntaxException
    {
      Component[] args;

      if (reader.canRead()) {
        args = argsParser.parse(reader).stream()
            .map(string -> Text.valueOf(string, context.viewer()))
            .toArray(Component[]::new);
      } else {
        args = new Component[0];
      }

      return Component.translatable(key, args);
    }
  }
}
