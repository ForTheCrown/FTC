package net.forthecrown.user;

public class UserOfflineException extends RuntimeException {

  public UserOfflineException(User user) {
    super(user.getName() + " is offline");  }
}