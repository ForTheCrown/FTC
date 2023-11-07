package net.forthecrown.guilds.waypoints;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import net.forthecrown.command.arguments.ParseResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildPermissions;
import net.forthecrown.utils.io.Results;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointExtension;
import net.forthecrown.waypoints.WaypointManager;
import org.bukkit.entity.Player;

public class GuildWaypointExtension implements WaypointExtension {

  private final GuildManager manager;

  public GuildWaypointExtension(GuildManager manager) {
    this.manager = manager;
  }

  @Override
  public Waypoint lookup(String name, WaypointManager manager) {
    Guild g = this.manager.getGuild(name);

    if (g == null) {
      return null;
    }

    return g.getSettings().getWaypoint();
  }

  @Override
  public DataResult<Unit> isValidName(String name) {
    if (manager.getGuild(name) == null) {
      return DataResult.success(Unit.INSTANCE);
    }

    return Results.error("Name belongs to a guild");
  }

  @Override
  public void addSuggestions(SuggestionsBuilder builder, CommandSource source) {
    // Suggest guilds
    Completions.suggest(builder,
        manager.getGuilds()
            .stream()
            .filter(guild -> {
              return guild.getSettings().getWaypoint() != null;
            })

            .map(Guild::getName)
    );
  }

  @Override
  public ParseResult<Waypoint> parse(StringReader reader) {
    var name = reader.readUnquotedString();

    // By guild name
    Guild guild = manager.getGuild(name);
    if (guild != null) {
      return new GuildResult(guild);
    }

    return null;
  }

  record GuildResult(Guild guild) implements ParseResult<Waypoint> {

    @Override
    public Waypoint get(CommandSource source, boolean validate) throws CommandSyntaxException {
      var waypoint = guild.getSettings().getWaypoint();

      if (waypoint == null) {
        throw GuildExceptions.noWaypoint(guild);
      }

      if (!validate
          || !source.isPlayer()
          || source.hasPermission(WPermissions.WAYPOINTS_ADMIN)
          || source.hasPermission(GuildPermissions.GUILD_ADMIN)
          || guild.getSettings().allowsVisit()
      ) {
        return waypoint;
      }

      Player player = source.asPlayer();

      if (guild.getMember(player.getUniqueId()) == null) {
        throw GuildExceptions.notGuildMember(guild);
      }

      return waypoint;
    }
  }
}
