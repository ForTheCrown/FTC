package net.forthecrown.text.placeholder;

import java.util.Map;
import java.util.function.Supplier;
import net.kyori.adventure.audience.Audience;

public interface PlaceholderList extends PlaceholderSource {

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
   * @param supplier Value supplier, supplier result will be turned into a component with
   *                 {@link net.forthecrown.text.Text#valueOf(Object, Audience)}
   * @return {@code this}
   * @see #add(String, TextPlaceholder)
   */
  PlaceholderList add(String name, Supplier<?> supplier);

  /**
   * Removes a placeholder with a specified {@code name}
   * @param name Placeholder name
   * @return {@code this}
   */
  PlaceholderList remove(String name);

  /**
   * Clears the placeholder list
   * @return {@code this}
   */
  PlaceholderList clear();

  /**
   * Gets the amount of placeholders inside this list
   * @return Placeholder count
   */
  int size();

  /**
   * Tests if this placeholder list is empty
   * @return {@code true}, if this list is empty, {@code false} otherwise
   */
  boolean isEmpty();

  /**
   * Gets an immutable map (name, placeholder) of the placeholders inside this list
   * @return Immutable placeholder map
   */
  Map<String, TextPlaceholder> getPlaceholders();
}
