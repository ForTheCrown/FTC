package net.forthecrown.core;

import java.util.Objects;
import net.forthecrown.FtcServer;
import net.forthecrown.Worlds;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.User;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class FtcServerImpl implements FtcServer {

  private Location serverSpawn;

  private final SettingsBook<User> globalSettings = new SettingsBook<>();

  @Override
  public @NotNull Location getServerSpawn() {
    if (serverSpawn == null) {
      World overworld = Worlds.overworld();
      return overworld.getSpawnLocation();
    }

    return serverSpawn.clone();
  }

  @Override
  public void setServerSpawn(@NotNull Location serverSpawn) {
    Objects.requireNonNull(serverSpawn);
    this.serverSpawn = serverSpawn.clone();
  }

  @Override
  public @NotNull SettingsBook<User> getGlobalSettingsBook() {
    return globalSettings;
  }
}