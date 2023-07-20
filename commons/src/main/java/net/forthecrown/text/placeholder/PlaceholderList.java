package net.forthecrown.text.placeholder;

import static net.kyori.adventure.text.Component.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
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
 * <br>
 * This will mean that placeholders registered via {@link #addDefault(String, TextPlaceholder)} will
 * also be used in the list
 *
 * @see TextPlaceholder
 * @see #render(Component, Audience)
 * @see #add(String, TextPlaceholder)
 * @see #usesDefaults()
 */
public interface PlaceholderList {

  /**
   * Creates a new placeholder list
   * @return Created list
   */
  static PlaceholderList newList() {
    return new PlaceholdersImpl();
  }

  static void addDefault(String name, TextPlaceholder placeholder) {
    Objects.requireNonNull(name, "Null name");
    Objects.requireNonNull(placeholder, "Null placeholder");

    PlaceholdersImpl.defaultPlaceholders.put(name, placeholder);
  }

  static void addDefault(String name, Object o) {
    addDefault(name, TextPlaceholder.simple(o));
  }

  static void addDefault(String name, Supplier<?> supplier) {
    addDefault(name, supplier);
  }

  static void removeDefault(String name) {
    PlaceholdersImpl.defaultPlaceholders.remove(name);
  }

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
  Component render(Component base, @Nullable Audience viewer);

  /**
   * Adds a placeholder
   * <p>
   * The name is used to refer to the placeholder in text, for example, if a placeholder named
   * 'version' was added, you would refer to it inside text like so:
   * {@code Current server version: ${version}}
   *
   * @param name Placeholder name
   * @param placeholder Placeholder
   * @return {@code this}
   */
  PlaceholderList add(String name, TextPlaceholder placeholder);

  /**
   * Adds a placeholder
   * @param name  Placeholder name
   * @param value Placeholder value, will be turned into a component with
   *              {@link net.forthecrown.text.Text#valueOf(Object, Audience)}
   * @return {@code this}
   * @see #add(String, TextPlaceholder)
   */
  PlaceholderList add(String name, Object value);

  /**
   * Adds a placeholder
   * @param name     Placeholder name
   * @param supplier Value supplier, will be turned into a component with
   *                 {@link net.forthecrown.text.Text#valueOf(Object, Audience)}
   * @return {@code this}
   * @see #add(String, TextPlaceholder)
   */
  PlaceholderList add(String name, Supplier<?> supplier);

  /**
   * Tells this list to use the default global placeholders
   * @return {@code this}
   * @see #usesDefaults()
   */
  PlaceholderList useDefaults();

  /**
   * Tests if this placeholder uses the default list of placeholders, by default, this will be
   * {@code false}
   *
   * @return {@code true}, if this list uses defaults, {@code false} otherwise
   */
  boolean usesDefaults();

  /**
   * Clears the placeholder list
   * @return {@code this}
   */
  PlaceholderList clear();

  /**
   * Gets an immutable map (name, placeholder) of the placeholders inside this list
   * @return Immutable placeholder map
   */
  Map<String, TextPlaceholder> getPlaceholders();
}
