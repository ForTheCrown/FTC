package net.forthecrown.waypoints.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;
import net.forthecrown.waypoints.Waypoint;

public interface WaypointFactory {

  void onCreate(User user) throws CommandSyntaxException;

  void postCreate(Waypoint waypoint, User user);
}