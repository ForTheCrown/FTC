package net.forthecrown.user.name;

import java.util.Set;
import net.forthecrown.text.TextWriter;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public interface UserNameFactory {

  /**
   * Fully formats a user's display name
   *
   * @param user The user whose name to format
   * @param viewer Audience viewing the display name
   * @param flags Displayname flags
   *
   * @return Fully formatted display name
   */
  default Component formatDisplayName(
      User user,
      @Nullable Audience viewer,
      Set<NameRenderFlags> flags
  ) {
    return formatDisplayName(user, createContext(user, viewer, flags));
  }

  /**
   * Fully formats a user's display name
   * @param user The user whose name to format
   * @param context Formatting context
   * @return Fully formatted display name
   */
  Component formatDisplayName(User user, DisplayContext context);

  @Nullable
  Component formatPrefix(User user, DisplayContext context);

  @Nullable
  Component formatSuffix(User user, DisplayContext context);

  /**
   * Formats the profile display.
   * <p>
   * This method assumes the viewer is allowed to view the specified {@code user}'s profile
   *
   * @param user User whose profile to format
   * @param viewer The audience viewing the profile
   *
   * @return Fully formatted profile display
   */
  Component formatProfileDisplay(User user, Audience viewer);

  /**
   * Formats the profile display.
   * <p>
   * This method assumes the viewer is allowed to view the specified {@code user}'s profile
   *
   * @param writer The writer to write the profile display to
   * @param user User whose profile to format
   * @param viewer The audience viewing the profile
   */
  default void writeProfileDisplay(TextWriter writer, User user, Audience viewer) {
    DisplayContext ctx = createContext(user, viewer, user.defaultRenderFlags());
    writeProfileDisplay(writer, user, ctx);
  }

  /**
   * Writes the user's profile display
   * @param writer Display output
   * @param user User whose profile to format
   * @param context Display context
   */
  void writeProfileDisplay(TextWriter writer, User user, DisplayContext context);

  /**
   * Applies the normal profile display style to the specified {@code writer}
   * @param writer Writer to apply the style to
   */
  void applyProfileStyle(TextWriter writer);

  /**
   * Creates a display context
   *
   * @param user User
   * @param viewer Audience viewer
   * @param flags DisplayName flags
   *
   * @return Created context
   */
  DisplayContext createContext(User user, Audience viewer, Set<NameRenderFlags> flags);

  /**
   * Adds a prefix name element
   * @param element Name element
   */
  void addPrefix(NameElement element);

  /**
   * Adds a suffix name element
   * @param element Name element
   */
  void addSuffix(NameElement element);

  /**
   * Adds a profile field. This element will be rendered in the user's hover event text or
   * whenever a player views a user's profile with {@code /profile}
   *
   * @param id Element ID
   * @param element Profile element
   */
  void addProfileField(String id, ProfileDisplayElement element);

  /**
   * Removes a profile display field
   * @param id Element ID
   */
  void removeField(String id);

  /**
   * Adds a profile field. This element will be rendered in the user's hover event text or
   * whenever a player views a user's profile with {@code /profile}.
   * <p>
   * This element will only be displayed if the profile viewer is an 'admin'
   *
   * @param id Element ID
   * @param element Profile element
   */
  void addAdminProfileField(String id, ProfileDisplayElement element);

  /**
   * Removes a profile display field
   * @param id Element ID
   */
  void removeAdminField(String id);
}