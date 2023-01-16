package net.forthecrown.user;

import java.io.Serial;

/**
 * An exception that's thrown when a method attempts to use an online only method in the User class
 * while the user is not online
 */
public class UserOfflineException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = -1;

  public UserOfflineException(User user) {
    super(user.getName() + " is not online");
  }
}