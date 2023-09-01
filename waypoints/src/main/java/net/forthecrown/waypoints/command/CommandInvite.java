package net.forthecrown.waypoints.command;

import static net.forthecrown.waypoints.WaypointPrefs.INVITES_ALLOWED;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.UserBlockList;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.WMessages;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoints;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;

public class CommandInvite extends FtcCommand {

  public CommandInvite() {
    super("Invite");

    setDescription("Invites users to your home waypoint");
    setPermission(WPermissions.WAYPOINTS);

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<users>", getDescription());
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("users", Arguments.ONLINE_USERS)
            .executes(c -> {
              var user = getUserSender(c);

              if (!user.get(INVITES_ALLOWED)) {
                throw Exceptions.format("You have inviting turned off");
              }

              var waypoint = Waypoints.getHomeWaypoint(user);

              if (waypoint == null) {
                throw WExceptions.NO_HOME_REGION;
              }

              var targets = Arguments.getUsers(c, "users");

              Optional<CommandSyntaxException> opt = UserBlockList.filterPlayers(
                  user,
                  targets,
                  INVITES_ALLOWED,
                  "{0, user} doesn't accept region invites",
                  Component.text("Cannot invite yourself")
              ).map(Exceptions::create);

              if (opt.isPresent()) {
                throw opt.get();
              }

              for (var target : targets) {
                waypoint.invite(user.getUniqueId(), target.getUniqueId());

                target.sendMessage(WMessages.targetInvited(user));
                target.playSound(Sound.UI_TOAST_IN, 2, 1.3f);
                user.sendMessage(WMessages.senderInvited(target));
              }

              if (targets.size() > 1) {
                user.sendMessage(WMessages.invitedTotal(targets.size()));
              }

              user.playSound(Sound.UI_TOAST_OUT, 2, 1.5f);

              return 0;
            })
        );
  }
}