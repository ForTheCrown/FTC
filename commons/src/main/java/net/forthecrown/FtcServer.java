package net.forthecrown;

import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.text.Text;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.User;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface FtcServer {

  static FtcServer server() {
    return ServiceInstances.getServer();
  }

  @NotNull
  Location getServerSpawn();

  void setServerSpawn(@NotNull Location serverSpawn);

  @NotNull
  SettingsBook<User> getGlobalSettingsBook();

  void announce(ViewerAwareMessage message);

  default void announce(ComponentLike like) {
    announce(viewer -> Text.valueOf(like, viewer));
  }

  void registerLeaveListener(String id, LeaveCommandListener listener);

  void unregisterLeaveListener(String id);

  interface LeaveCommandListener {
    boolean onUse(User player);
  }
}