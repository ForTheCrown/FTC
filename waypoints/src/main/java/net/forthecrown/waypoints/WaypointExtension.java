package net.forthecrown.waypoints;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import net.forthecrown.command.arguments.ParseResult;
import net.forthecrown.grenadier.CommandSource;

public interface WaypointExtension {

  default Waypoint lookup(String name, WaypointManager manager) {
    return null;
  }

  default DataResult<Unit> isValidName(String name) {
    return DataResult.success(Unit.INSTANCE);
  }

  default void addSuggestions(SuggestionsBuilder builder, CommandSource source) {

  }

  default ParseResult<Waypoint> parse(StringReader reader) throws CommandSyntaxException {
    return null;
  }
}
