package net.forthecrown.user;

import org.jetbrains.annotations.NotNull;

public interface UserBlockList extends UserComponent {

  @NotNull User getUser();

  @NotNull IgnoreResult testIgnored(@NotNull User other);

  default boolean isBlocked(@NotNull User other) {
    return testIgnored(other).isBlocked();
  }

  void setIgnored(@NotNull User other, boolean separated);

  void removeIgnored(@NotNull User other);

  void removeSeparated(@NotNull User other);

  enum IgnoreResult {
    NOT_IGNORED,
    SEPARATED,
    BLOCKED;

    public boolean isBlocked() {
      return this != NOT_IGNORED;
    }
  }
}