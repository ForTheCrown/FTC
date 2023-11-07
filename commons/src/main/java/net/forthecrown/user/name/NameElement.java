package net.forthecrown.user.name;

import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Element within a name
 */
public interface NameElement {

  /**
   * Writes this element's data to the specified writer
   *
   * @param user    User whose name is being formatted
   * @param context Display context
   */
  @Nullable
  Component createDisplay(User user, DisplayContext context);
}
