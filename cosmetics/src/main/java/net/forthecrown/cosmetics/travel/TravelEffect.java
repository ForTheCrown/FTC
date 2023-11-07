package net.forthecrown.cosmetics.travel;

import net.forthecrown.user.User;
import org.bukkit.Location;

public interface TravelEffect {

  void onPoleTeleport(User user, Location from, Location to);

  void onHulkStart(User user, Location loc);

  void onHulkTickDown(User user, Location loc);

  void onHulkTickUp(User user, Location loc);

  void onHulkLand(User user, Location loc);
}