package net.forthecrown.user.name;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import org.bukkit.permissions.Permission;

public record DisplayContext(
    Audience viewer,
    boolean useNickName,
    boolean userOnline,
    boolean self,
    DisplayIntent intent
) {

  public DisplayContext withIntent(DisplayIntent intent) {
    return new DisplayContext(viewer, useNickName, userOnline, self, intent);
  }

  public boolean intentMatches(DisplayIntent... intents) {
    for (var i : intents) {
      if (this.intent == i) {
        return true;
      }
    }

    return false;
  }

  public <T> T viewerProperty(UserProperty<T> property) {
    return viewerUser().map(user -> user.get(property)).orElseGet(property::getDefaultValue);
  }

  public Optional<User> viewerUser() {
    return Optional.ofNullable(Audiences.getPlayer(viewer)).map(Users::get);
  }

  public boolean viewerHasPermission(String permission) {
    return viewerUser()
        .map(user -> user.hasPermission(permission))
        .orElse(false);
  }

  public boolean viewerHasPermission(Permission permission) {
    return viewerHasPermission(permission.getName());
  }

  public Set<NameRenderFlags> flagSet() {
    Set<NameRenderFlags> flags = EnumSet.noneOf(NameRenderFlags.class);
    if (useNickName) {
      flags.add(NameRenderFlags.ALLOW_NICKNAME);
    }
    if (userOnline) {
      flags.add(NameRenderFlags.USER_ONLINE);
    }
    return flags;
  }
}
