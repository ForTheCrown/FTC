package net.forthecrown.waypoints.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.ParseResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.Users;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointHomes;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.Waypoints;

public interface WaypointResults {
  /* ----------------------------- CONSTANTS ------------------------------ */

  ParseResult<Waypoint> NEAREST = (source, validate) -> {
    if (validate && !source.hasPermission(WPermissions.WAYPOINTS_FLAGS)) {
      throw Exceptions.NO_PERMISSION;
    }

    var player = source.asPlayer();
    Waypoint waypoint = Waypoints.getNearest(Users.get(player));

    if (waypoint == null) {
      throw WExceptions.FAR_FROM_WAYPOINT;
    }

    return waypoint;
  };

  ParseResult<Waypoint> CURRENT = (source, validate) -> {
    if (validate && !source.hasPermission(WPermissions.WAYPOINTS_FLAGS)) {
      throw Exceptions.NO_PERMISSION;
    }

    var player = source.asPlayer();
    Waypoint waypoint = Waypoints.getNearest(Users.get(player));

    if (waypoint == null) {
      throw WExceptions.FAR_FROM_WAYPOINT;
    }

    if (!waypoint.getBounds().contains(player)) {
      throw WExceptions.farFromWaypoint(waypoint);
    }

    return waypoint;
  };

  /* ------------------------------ STATICS ------------------------------- */

  static boolean isAccessInvalid(CommandSource source, Waypoint waypoint) {
    if (!source.isPlayer() || source.hasPermission(WPermissions.WAYPOINTS_ADMIN)) {
      return false;
    }

    var player = source.asPlayerOrNull();

    return !waypoint.hasValidInvite(player.getUniqueId())
        && !waypoint.isResident(player.getUniqueId());
  }

  /* -------------------------- IMPLEMENTATIONS --------------------------- */

  record UserResult(LookupEntry lookup) implements ParseResult<Waypoint> {

    @Override
    public Waypoint get(CommandSource source, boolean shouldValidate)
        throws CommandSyntaxException
    {
      User user = Users.get(lookup);
      boolean self = source.textName().equals(user.getName());

      Optional<Waypoint> waypoint = WaypointHomes.getHome(user);

      if (waypoint.isEmpty()) {
        if (self) {
          throw WExceptions.NO_HOME_REGION;
        } else {
          throw WExceptions.noHomeWaypoint(user);
        }
      }

      if (source.isPlayer()) {
        var senderUser = Users.get(source.asPlayer());

        UserBlockList.testBlockedException(senderUser, user,
            Messages.BLOCKED_SENDER,
            Messages.BLOCKED_TARGET
        );
      }

      if (shouldValidate && !self && isAccessInvalid(source, waypoint.get())) {
        throw WExceptions.notInvited(user);
      }

      return waypoint.get();
    }
  }

  record DirectResult(Waypoint waypoint) implements ParseResult<Waypoint> {

    @Override
    public Waypoint get(CommandSource source, boolean shouldValidate)
        throws CommandSyntaxException
    {
      if (shouldValidate
          && isAccessInvalid(source, waypoint)
          && !waypoint.get(WaypointProperties.PUBLIC)
      ) {
        throw WExceptions.privateRegion(waypoint);
      }

      return waypoint;
    }
  }
}