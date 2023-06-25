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
import net.forthecrown.waypoints.event.WaypointParseEvent;
import net.forthecrown.waypoints.event.WaypointSuggestionsEvent;

public class WaypointArgument implements ArgumentType<ParseResult<Waypoint>> {

  public static final String
      FLAG_CURRENT = "-current",
      FLAG_NEAREST = "-nearest";

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
    Waypoint waypoint = WaypointManager.getInstance().get(name);
    if (waypoint != null) {
      return new WaypointResults.DirectResult(waypoint);
    }

    // By username
    LookupEntry lookup = Users.getService().getLookup().query(name);

    if (lookup != null) {
      return new WaypointResults.UserResult(lookup);
    }

    // By guild name
    WaypointParseEvent event = new WaypointParseEvent(Readers.copy(reader, start));
    event.callEvent();

    if (event.isCancelled()) {
      throw event.getException();
    }

    if (event.getParseResult() != null) {
      reader.setCursor(event.getReader().getCursor());
      return event.getParseResult();
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

    WaypointSuggestionsEvent event
        = new WaypointSuggestionsEvent((CommandContext<CommandSource>) context, builder);

    event.callEvent();

    return Completions.suggest(
        event.getBuilder(),
        WaypointManager.getInstance().getNames()
    );
  }
}