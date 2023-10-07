package net.forthecrown.text.placeholder;

import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * A list of placeholders used to render a text that contains placeholders.
 *
 * <p>
 * Placeholders look like so: {@code ${placeholder}}, you can additionally specify more information
 * for the placeholder to use like so: {@code ${placeholder: extra information}}. If the placeholder
 * doesn't require extra information, then that should be ignored by the placeholder.
 * <br>
 * However, if the placeholder requires more information, then {@code null} should be returned,
 * which will tell the renderer that the placeholder input should be returned
 *
 * <p>
 * Additionally, this list also supports global placeholders. These placeholders are, by default,
 * disabled for each created list. To enable them, use {@link #useDefaults()}.
 *
 * <h3>Placeholder Syntax</h3>
 * Placeholders follow a simple syntax:
 * <pre>
 * Placeholder
 *   : '${' PlaceholderName '}'
 *   | '${' PlaceholderName ':' PlaceholderInput '}'
 *   ;
 *
 * PlaceholderName: [a-zA-Z0-9_$./-+]+
 * PlaceholderInput: A string that is terminated by a non-escaped '}' character
 * </pre>
 *
 * @see TextPlaceholder
 * @see #render(Component, Audience)
 */
public interface PlaceholderRenderer extends PlaceholderList {

  Pattern PATTERN
      = Pattern.compile("\\\\?\\$\\{([a-zA-Z0-9_$./\\-+]+)(?:: *((?:\\\\}|[^}])+))?\\}");

  default Component render(Component base) {
    return render(base, null);
  }

  /**
   * Renders the placeholders inside the specified {@code base}
   *
   * @param base Base component to render
   * @param viewer Viewer viewing the message, may be {@code null}
   * @return The rendered component
   */
  default Component render(Component base, @Nullable Audience viewer) {
    return render(base, viewer, Map.of());
  }

  /**
   * Renders the placeholders inside the specified {@code base}
   *
   * @param base Base text to render
   * @param viewer Viewer viewing the message, may be {@code null}
   * @param ctx Rendering context
   *
   * @return Rendered component
   */
  Component render(Component base, @Nullable Audience viewer, Map<String, Object> context);

  /**
   * Tells this list to use the default global placeholders
   * @return {@code this}
   */
  PlaceholderRenderer useDefaults();

  PlaceholderRenderer addSource(PlaceholderSource source);

  PlaceholderList getPlaceholderList();

  default PlaceholderRenderer add(String name, TextPlaceholder placeholder) {
    getPlaceholderList().add(name, placeholder);
    return this;
  }

  default PlaceholderRenderer add(String name, Supplier<?> placeholder) {
    getPlaceholderList().add(name, placeholder);
    return this;
  }

  default PlaceholderRenderer add(String name, Object placeholder) {
    getPlaceholderList().add(name, placeholder);
    return this;
  }

  @Override
  default PlaceholderRenderer remove(String name) {
    getPlaceholderList().remove(name);
    return this;
  }

  @Override
  default PlaceholderRenderer clear() {
    getPlaceholderList().clear();
    return this;
  }

  @Override
  default int size() {
    return getPlaceholderList().size();
  }

  @Override
  default boolean isEmpty() {
    return getPlaceholderList().isEmpty();
  }

  @Override
  default Map<String, TextPlaceholder> getPlaceholders() {
    return getPlaceholderList().getPlaceholders();
  }
}
