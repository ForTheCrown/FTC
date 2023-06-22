package net.forthecrown;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface ServerSpawn {

  static ServerSpawn spawn() {
    return BukkitServices.loadOrThrow(ServerSpawn.class);
  }

  @NotNull Location get();

  void set(@NotNull Location location);
}