package net.forthecrown.text.format;

import static net.forthecrown.text.format.TextFormatTypes.DEFAULT;
import static net.forthecrown.text.format.TextFormatTypes.formatTypes;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.text.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * A utility class for formatting components. Similar to {@link java.text.MessageFormat} except way
 * less functional, and tailored towards components instead of strings.
 */
@RequiredArgsConstructor
public class ComponentFormat implements ComponentLike {

  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * The replacement pattern to use for component's, broadly, it's any text that's enclosed by '{}'
   * chars
   */
  private static final Pattern REPLACE_PATTERN
      = Pattern.compile("\\{ *([0-9]+) *(?:, *([a-zA-Z0-9_/.]+))?(?: *, *([^}]+))?}");

  private static final Logger LOGGER = Loggers.getLogger();

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  private final Component format;
  private final Object[] args;
  private final Audience audience;

  /* ----------------------------- METHODS ------------------------------ */

  /**
   * Formats the given {@link #format}. This will replace the arguments in any part of the text,
   * including in children, and even in {@link net.kyori.adventure.text.TranslatableComponent}
   * arguments.
   *
   * @return The formatted component
   * @throws IllegalStateException Will be thrown if the formatter couldn't parse a given argument
   *                               correctly.
   */
  @Override
  public @NotNull Component asComponent() throws IllegalStateException {
    // Create a replacement config and then
    // apply it to the given base format
    return format.replaceText(
        TextReplacementConfig.builder()
            .match(REPLACE_PATTERN)

            // Replacement function which will replace
            // any given arguments in the text
            .replacement((match, builder) -> formatArgument(match))
            .build()
    );
  }

  /**
   * Formats the given argument.
   * <p>
   * If this fails to parse a given argument match result then it simply returns the given input in
   * component form
   *
   * @param result The argument match result
   * @return The formatted argument
   */
  private Component formatArgument(MatchResult result) {
    String indexInput = result.group(1);
    String name = result.group(2);
    String style = result.group(3);

    if (style == null) {
      style = "";
    }

    TextFormatType type;

    if (Strings.isNullOrEmpty(name)) {
      type = DEFAULT;
    } else {
      var opt = formatTypes.get(name);

      if (opt.isEmpty()) {
        LOGGER.warn("Unknown ComponentFormat type '{}'", name);
        type = DEFAULT;
      } else {
        type = opt.get();
      }
    }

    int index = Integer.parseInt(indexInput);

    if (index < 0 || index >= args.length) {
      LOGGER.error("Index {} out of range 0 to size {}, format='{}'",
          index, args.length, Text.plain(format),
          new Throwable()
      );

      return text("[Index error (" + index + " of size " + args.length + ")]");
    }

    Object value = args[index];
    return type.resolve(value, style, audience);
  }
}