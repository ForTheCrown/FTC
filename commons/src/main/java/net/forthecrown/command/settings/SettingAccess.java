package net.forthecrown.command.settings;

import java.util.Objects;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;

public interface SettingAccess {

  static SettingAccess property(UserProperty<Boolean> property) {
    Objects.requireNonNull(property);

    return new SettingAccess() {
      @Override
      public boolean getState(User user) {
        return user.get(property);
      }

      @Override
      public void setState(User user, boolean state) {
        user.set(property, state);
      }
    };
  }

  boolean getState(User user);

  void setState(User user, boolean state);
}