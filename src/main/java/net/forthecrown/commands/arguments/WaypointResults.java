package net.forthecrown.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.Users;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.Waypoints;
import org.bukkit.entity.Player;

import java.util.Objects;

public interface WaypointResults {
    /* ----------------------------- CONSTANTS ------------------------------ */

    ParseResult<Waypoint> NEAREST = (source, validate) -> {
        if (validate && !source.hasPermission(Permissions.WAYPOINTS_FLAGS)) {
            throw Exceptions.NO_PERMISSION;
        }

        var player = source.asPlayer();
        Waypoint waypoint = Waypoints.getNearest(Users.get(player));

        if (waypoint == null) {
            throw Exceptions.FAR_FROM_WAYPOINT;
        }

        return waypoint;
    };

    ParseResult<Waypoint> CURRENT = (source, validate) -> {
        if (validate && !source.hasPermission(Permissions.WAYPOINTS_FLAGS)) {
            throw Exceptions.NO_PERMISSION;
        }

        var player = source.asPlayer();
        Waypoint waypoint = Waypoints.getNearest(Users.get(player));

        if (waypoint == null) {
            throw Exceptions.FAR_FROM_WAYPOINT;
        }

        if (!waypoint.getBounds().contains(player)) {
            throw Exceptions.farFromWaypoint(waypoint);
        }

        return waypoint;
    };

    /* ------------------------------ STATICS ------------------------------- */

    static boolean isAccessInvalid(CommandSource source, Waypoint waypoint) {
        if (!source.isPlayer()
                || source.hasPermission(Permissions.WAYPOINTS_ADMIN)
        ) {
            return false;
        }

        var player = source.asPlayerOrNull();

        return !waypoint.hasValidInvite(player.getUniqueId())
                && !waypoint.isResident(player.getUniqueId());
    }

    /* -------------------------- IMPLEMENTATIONS --------------------------- */

    record UserResult(UserLookupEntry lookup) implements ParseResult<Waypoint> {
        @Override
        public Waypoint get(CommandSource source, boolean shouldValidate)
                throws CommandSyntaxException
        {
            User user = Users.get(lookup);
            boolean self = source.textName().equals(user.getName());

            Waypoint waypoint = user.getHomes().getHomeTeleport();

            if (waypoint == null) {
                if (self) {
                    throw Exceptions.NO_HOME_REGION;
                } else {
                    throw Exceptions.noHomeWaypoint(user);
                }
            }

            if (source.isPlayer()) {
                var senderUser = Users.get(source.asPlayer());

                Users.testBlockedException(senderUser, user,
                        Messages.VISIT_BLOCKED_SENDER,
                        Messages.VISIT_BLOCKED_TARGET
                );

                if (Objects.equals(
                        senderUser.getUniqueId(),
                        user.getInteractions().getSpouse())
                ) {
                    return waypoint;
                }
            }

            if (shouldValidate
                    && !self
                    && isAccessInvalid(source, waypoint)
            ) {
                throw Exceptions.notInvited(user);
            }

            return waypoint;
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
                throw Exceptions.privateRegion(waypoint);
            }

            return waypoint;
        }
    }

    record GuildResult(Guild guild) implements ParseResult<Waypoint> {
        @Override
        public Waypoint get(CommandSource source, boolean validate) throws CommandSyntaxException {
            var waypoint = guild.getSettings().getWaypoint();

            if (waypoint == null) {
                throw Exceptions.noWaypoint(guild);
            }

            if (!validate
                    || !source.isPlayer()
                    || source.hasPermission(Permissions.WAYPOINTS_ADMIN)
                    || source.hasPermission(Permissions.GUILD_ADMIN)
                    || guild.getSettings().allowsVisit()
            ) {
                return waypoint;
            }

            Player player = source.asPlayer();

            if (guild.getMember(player.getUniqueId()) == null) {
                throw Exceptions.notGuildMember(guild);
            }

            return waypoint;
        }
    }
}