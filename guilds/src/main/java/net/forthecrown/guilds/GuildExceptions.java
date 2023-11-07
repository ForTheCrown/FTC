package net.forthecrown.guilds;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.Waypoint;

public interface GuildExceptions {

  CommandSyntaxException NOT_A_BANNER = create("Not a banner!");

  CommandSyntaxException ALREADY_IN_GUILD = create("You are already in a guild");

  CommandSyntaxException GLEADER_CANNOT_LEAVE
      = create("Leader cannot leave their own guild\nUse '/g delete' to delete the guild");

  CommandSyntaxException NOT_IN_GUILD = create("You are not in a guild");

  CommandSyntaxException CANNOT_CLAIM_CHUNKS = create("You do not have permission to claim chunks");

  CommandSyntaxException GUILDS_WRONG_WORLD = create("Guilds do not exist in this world");

  CommandSyntaxException PROMOTE_SELF = create("Cannot promote self");

  CommandSyntaxException PROMOTE_LEADER = create("Cannot promote guild leader");

  CommandSyntaxException DEMOTE_LEADER = create("Cannot demote leader");

  CommandSyntaxException DEMOTE_SELF = create("Cannot demote self");

  CommandSyntaxException KICK_SELF = create("Cannot kick yourself lol");

  CommandSyntaxException CANNOT_KICK_LEADER = create("Cannot kick guild leader");

  CommandSyntaxException G_NO_PERM_WAYPOINT = create(
      "Cannot change guild waypoint! You do not have permission"
  );

  CommandSyntaxException G_EXTERNAL_WAYPOINT = create(
      "The chunk the waypoint is in is not claimed by your guild"
  );

  static CommandSyntaxException waypointAlreadySet(Waypoint existing) {
    return WExceptions.waypointAlreadySet(existing, "Your guild already has a waypoint", "guild");
  }

  static CommandSyntaxException guildNameSmall(String name) {
    return format("'{0}' is too small for a guild name. Minimum {1, number} characters",
        name, Guild.MIN_NAME_SIZE
    );
  }

  static CommandSyntaxException guildNameLarge(String name) {
    return format("'{0}' is too large for a guild name. Maximum {1, number} characters",
        name, Guild.MAX_NAME_SIZE
    );
  }

  static CommandSyntaxException cannotClaimMoreChunks(Guild guild, int max) {
    return format("{0} Cannot claim more than {1, number} chunks", guild.displayName(), max);
  }

  static CommandSyntaxException chunkAlreadyClaimed(Guild owner) {
    return format("{0} has already claimed this chunk", owner.displayName());
  }

  static CommandSyntaxException notARank(int rank) {
    return format("{0, number} is not a valid rank", rank);
  }

  static CommandSyntaxException cannotUnclaimChunk(Guild guild) {
    return format("Cannot unclaim! {0} does not own the chunk", guild.displayName());
  }

  static CommandSyntaxException cannotPromote(User user) {
    return format("Cannot promote {0, user} further", user);
  }

  static CommandSyntaxException cannotDemote(User user) {
    return format("Cannot demote {0, user} further", user);
  }

  static CommandSyntaxException notGuildMember(User user, Guild guild) {
    return format("{0, user} is not a member of the {1} guild", user, guild.displayName());
  }

  static CommandSyntaxException notGuildMember(Guild guild) {
    return format("You are not a member of the {0} guild", guild.displayName());
  }

  static CommandSyntaxException noWaypoint(Guild guild) {
    return format("{0} has no set waypoint.", guild.displayName());
  }

  static CommandSyntaxException guildFull(Guild guild) {
    return format("{0} is full and cannot accept more members", guild.displayName());
  }
}
