package net.forthecrown;

import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.User;
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
}