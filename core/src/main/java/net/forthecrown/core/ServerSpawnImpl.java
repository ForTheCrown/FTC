package net.forthecrown.core;

import java.util.Objects;
import net.forthecrown.ServerSpawn;
import net.forthecrown.utils.Locations;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class ServerSpawnImpl implements ServerSpawn {

  private Location location;

  @Override
  public @NotNull Location get() {
    return location;
  }

  @Override
  public void set(@NotNull Location location) {
    Objects.requireNonNull(location);
    this.location = Locations.clone(location);
  }
}