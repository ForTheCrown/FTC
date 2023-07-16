package net.forthecrown.text;

import io.papermc.paper.chat.ChatRenderer.ViewerUnaware;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A message that requires a viewer to be fully rendered
 */
public interface ViewerAwareMessage extends ComponentLike {

  /**
   * Wraps a component
   * @param component Text to wrap
   * @return Wrapped text
   */
  static ViewerAwareMessage wrap(Component component) {
    return viewer -> component;
  }

  /**
   * Fully renders the message using the specified viewer
   * @param viewer Text viewer, or {@code null}, if not viewer was specified
   * @return Rendered text
   */
  Component create(@Nullable Audience viewer);

  @Override
  @NotNull
  default Component asComponent() {
    return create(null);
  }
}
