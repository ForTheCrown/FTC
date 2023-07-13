package net.forthecrown.user;

import java.util.Collection;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * List of blocked/separated users
 */
public interface UserBlockList extends UserComponent {

  /**
   * User this block list component is bound to
   * @return List owner
   */
  @NotNull User getUser();

  /**
   * Tests if the specified {@code other} user is ignored or separated
   * @param other User to test
   * @return Ignore result
   */
  @NotNull IgnoreResult testIgnored(@NotNull User other);

  /**
   * Tests if the result {@link #testIgnored(User)} is {@link IgnoreResult#BLOCKED} or
   * {@link IgnoreResult#SEPARATED}
   *
   * @param other User to test
   * @return {@code true}, if the specified {@code other} is blocked or separated,
   *         {@code false} otherwise
   */
  default boolean isBlocked(@NotNull User other) {
    return testIgnored(other).isBlocked();
  }

  /**
   * Sets a specified {@code other} user as ignored or separated.
   * <p>
   * <b>Note:</b> The {@code separated} parameter only determines which of the 2 block lists the
   * specified {@code other} user is placed into, it will not modify the other user's block list
   *
   * @param other User to ignore/separate
   * @param separated {@code true}, if the user is being forcefully blocked
   */
  void setIgnored(@NotNull User other, boolean separated);

  /**
   * Removes the specified {@code other} from this user's blocklist
   * @param other Other user
   */
  void removeIgnored(@NotNull User other);

  /**
   * Removes the specified {@code other} from this user's separated users list.
   * <p>
   * This method will not modify the {@code other} user's blocklist, so to fully remove the forced
   * separation between 2 users, this method must be called for the blocklist of both users
   *
   * @param other Other user
   */
  void removeSeparated(@NotNull User other);

  /**
   * Gets an immutable collection of blocked user IDs
   * @return Blocked user ID set
   */
  @NotNull
  Collection<UUID> getBlocked();

  /**
   * Gets an immutable collection of user IDs that this user has been separated from
   * @return Separated user ID set
   */
  @NotNull
  Collection<UUID> getSeparated();

  /**
   * Value returned by {@link #testIgnored(User)}
   */
  enum IgnoreResult {

    /**
     * Users are not separated and have not blocked each other in any capacity
     */
    NOT_IGNORED,

    /**
     * Users are forcefully separated
     */
    SEPARATED,

    /**
     * User has blocked the target character willingly
     */
    BLOCKED;

    public boolean isBlocked() {
      return this != NOT_IGNORED;
    }
  }
}