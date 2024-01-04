package net.forthecrown.waypoints.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.FtcSuggestions;
import net.forthecrown.command.arguments.ParseResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.Users;
import net.forthecrown.waypoints.WExceptions;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;

public class WaypointArgument implements ArgumentType<ParseResult<Waypoint>> {

  public static final String FLAG_CURRENT = "-current";
  public static final String FLAG_NEAREST = "-nearest";

  @Override
  public ParseResult<Waypoint> parse(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();
    var name = reader.readUnquotedString();

    // By flags
    if (name.equalsIgnoreCase(FLAG_CURRENT)) {
      return WaypointResults.CURRENT;
    } else if (name.equalsIgnoreCase(FLAG_NEAREST)) {
      return WaypointResults.NEAREST;
    }

    // By waypoint name
    WaypointManager manager = WaypointManager.getInstance();
    Waypoint waypoint = manager.get(name);
    if (waypoint != null) {
      return new WaypointResults.DirectResult(waypoint);
    }

    // By username
    LookupEntry lookup = Users.getService().getLookup().query(name);
    if (lookup != null) {
      return new WaypointResults.UserResult(lookup);
    }

    // By guild name
    var extensions = manager.getExtensions();

    for (var e: extensions) {
      try {
        var extReader = Readers.copy(reader, start);
        ParseResult<Waypoint> result = e.parse(extReader);

        if (result != null) {
          return result;
        }
      } catch (CommandSyntaxException exc) {
        continue;
      }
    }

    throw WExceptions.unknownRegion(reader, start);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    CommandSource source = (CommandSource) context.getSource();

    if (source.hasPermission(WPermissions.WAYPOINTS_FLAGS)) {
      Completions.suggest(builder, FLAG_NEAREST, FLAG_CURRENT);
    }

    // Suggest players
    FtcSuggestions.suggestPlayerNames(source, builder, false);

    WaypointManager manager = WaypointManager.getInstance();

    var extensions = manager.getExtensions();
    extensions.forEach(extension -> extension.addSuggestions(builder, source));

    return Completions.suggest(builder, manager.getNames());
  }
}