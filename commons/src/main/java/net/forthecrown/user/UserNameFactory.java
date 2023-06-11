package net.forthecrown.user;

import java.util.Optional;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.TextWriter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface UserNameFactory {

  /* ----------------------------- FLAGS ------------------------------ */

  /**
   * Flag for {@link #formatDisplayName(User, Audience, int)} to allow for nicknames to be used
   */
  int ALLOW_NICKNAME  = 0b0000001;

  /**
   * Flag for {@link #formatDisplayName(User, Audience, int)} which specifies that the name is
   * being formatted for a hover text context.
   * <p>
   * Required to prevent formatting the hover text for users in the resulting user display name,
   * as this behaviour can cause recursive name formatting that causes a stack overflow exception
   */
  int FOR_HOVER       = 0b0000010;

  /**
   * Flag for {@link #formatDisplayName(User, Audience, int)} which specifies that the name is
   * being formatted for a join/leave message
   */
  int JOIN_MESSAGE    = 0b0000100;

  /* ----------------------------- METHODS ------------------------------ */

  /**
   * Fully formats a user's display name
   *
   * @param user The user whose name to format
   * @param viewer Audience viewing the display name
   * @param flags Displayname flags
   *
   * @return Fully formatted display name
   */
  Component formatDisplayName(User user, @Nullable Audience viewer, int flags);

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
   * Creates a display context
   *
   * @param user User
   * @param viewer Audience viewer
   * @param flags DisplayName flags
   *
   * @return Created context
   */
  DisplayContext createContext(User user, Audience viewer, int flags);

  /**
   * Adds a prefix name element
   * @param id Element ID
   * @param element Name element
   */
  void addPrefix(NameElement element);

  /**
   * Adds a suffix name element
   * @param id Element ID
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

  /* ----------------------------- SUB CLASSES ------------------------------ */

  /**
   * Element within a name
   */
  interface NameElement {

    /**
     * Writes this element's data to the specified writer
     * @param user User whose name is being formatted
     * @param context Display context
     */
    @Nullable
    Component createDisplay(User user, DisplayContext context);
  }

  /**
   * Element within a user's hover text or fully formatted profile display
   */
  interface ProfileDisplayElement {

    /**
     * Writes this element's data to the specified writer
     * @param writer Output writer
     * @param user User whose name is being formatted
     * @param context Display context
     */
    void write(TextWriter writer, User user, DisplayContext context);

    /**
     * This field's placement. Determines where the field is displayed, either in both the hover
     * text and regular profile view, or in 1 of those exclusively
     *
     * @return Field placement
     */
    default FieldPlacement placement() {
      return FieldPlacement.IN_PROFILE;
    }
  }


  enum FieldPlacement {
    ALL,
    IN_HOVER,
    IN_PROFILE
  }

  record DisplayContext(
      Audience viewer,
      boolean useNickName,
      boolean forHover,
      boolean joinMessage,
      boolean self
  ) {

    public Optional<User> viewerUser() {
      return userFromAudience(viewer);
    }

    public boolean viewerHasPermission(String permission) {
      return viewerUser()
          .map(user -> user.hasPermission(permission))
          .orElse(false);
    }

    public static Optional<User> userFromAudience(Audience viewer) {
      if (viewer instanceof User user) {
        return Optional.of(user);
      }

      if (viewer instanceof Player player) {
        return Optional.of(Users.get(player));
      }

      if (viewer instanceof CommandSource source) {
        return userFromAudience(source.audience());
      }

      return Optional.empty();
    }
  }
}