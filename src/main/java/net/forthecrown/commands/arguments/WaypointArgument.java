package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.UserManager;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointManager;

import java.util.concurrent.CompletableFuture;

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
        UserLookupEntry lookup = UserManager.get()
                .getUserLookup()
                .get(name);

        if (lookup != null) {
            return new WaypointResults.UserResult(lookup);
        }

        // By guild name
        Guild guild = GuildManager.get().getGuild(name);
        if (guild != null) {
            return new WaypointResults.GuildResult(guild);
        }

        throw Exceptions.unknownRegion(reader, start);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CommandSource source = (CommandSource) context.getSource();

        if (source.hasPermission(Permissions.WAYPOINTS_FLAGS)) {
            CompletionProvider.suggestMatching(builder,
                    FLAG_NEAREST, FLAG_CURRENT
            );
        }

        // Suggest players
        FtcSuggestions.suggestPlayerNames(source, builder, true);

        // Suggest guilds
        CompletionProvider.suggestMatching(builder,
                GuildManager.get().getGuilds()
                        .stream()
                        .filter(guild -> {
                            return guild.getSettings().getWaypoint() != null;
                        })

                        .map(Guild::getName)
        );

        return CompletionProvider.suggestMatching(
                builder,
                WaypointManager.getInstance().getNames()
        );
    }
}