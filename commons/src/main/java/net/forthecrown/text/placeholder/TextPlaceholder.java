package net.forthecrown.text.placeholder;

import java.util.function.Supplier;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Functional interface that renders instances of placeholders in text
 */
@FunctionalInterface
public interface TextPlaceholder {

  static TextPlaceholder simple(Object component) {
    return (match, ctx) -> Text.valueOf(component, ctx.viewer());
  }

  static TextPlaceholder simple(Supplier<?> supplier) {
    return (match, ctx) -> Text.valueOf(supplier.get(), ctx.viewer());
  }

  /**
   * Renders an instance of a placeholder
   * <p>
   * Match parameter will work as follows:
   * <table>
   *   <thead>
   *     <tr>
   *       <th>Input</th>
   *       <th>{@code match} parameter</th>
   *     </tr>
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <td>{@code ${placeHolderName}}</td>
   *       <td>An empty string</td>
   *     </tr>
   *     <tr>
   *       <td>{@code ${placeHolderName: match}}</td>
   *       <td>"match"</td>
   *     </tr>
   *     <tr>
   *       <td>{@code ${placeHolderName: }}</td>
   *       <td>Won't render</td>
   *     </tr>
   *     <tr>
   *       <td>{@code ${placeHolderName: {Another set of braces\}}}</td>
   *       <td>"{Another set of braces}" Braces can be escaped</td>
   *     </tr>
   *   </tbody>
   * </table>
   *
   *
   * @param match The given placeholder input
   * @param render The context to the message being rendered
   * @return The rendered placeholder, or {@code null}, to just display the input text
   */
  @Nullable
  Component render(String match, PlaceholderContext render);
}
