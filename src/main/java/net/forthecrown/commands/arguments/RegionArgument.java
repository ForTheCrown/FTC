package net.forthecrown.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import org.spongepowered.math.vector.Vector2i;

import java.util.concurrent.CompletableFuture;

public class RegionArgument implements ArgumentType<RegionParseResult>, VanillaMappedArgument {
    RegionArgument() {}

    @Override
    public RegionParseResult parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        //Try getting it by name and returning that
        RegionParseResult initialResult = parseByName(reader);
        if (initialResult != null) {
            return initialResult;
        }

        //Try getting it from a user parse.
        try {
            UserArgument argument = Arguments.ONLINE_USER;
            UserParseResult result = argument.parse(reader);

            return new RegionParseResult(result);
        } catch (CommandSyntaxException e) {
            //User parse failed, it is an unknown region
            throw Exceptions.unknownRegion(reader, cursor);
        }
    }

    private RegionParseResult parseByName(StringReader origReader) {
        //Clone the reader so we could parse a user from the same point later
        //if this fails
        StringReader reader = new StringReader(origReader.getString());
        reader.setCursor(origReader.getCursor());

        //Read a name and get region from that
        String name = reader.readUnquotedString();
        PopulationRegion region = RegionManager.get().get(name);

        //Region doesn't exist
        if (region == null) {
            return null;
        }

        //Region exists, modify original reader and return result
        origReader.setCursor(reader.getCursor());
        return new RegionParseResult(region);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        // List players and target selector
        FtcSuggestions.suggestPlayerNames((CommandSource) context.getSource(), builder, false);

        //List regions
        String token = builder.getRemainingLowerCase();

        for (PopulationRegion r: RegionManager.get().getNamedRegions()) {
            if (CompletionProvider.startsWith(token, r.getName())) {
                builder.suggest(r.getName(), suggestionTooltip(r));
            }
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.string();
    }

    private static Message suggestionTooltip(PopulationRegion region) {
        Vector2i polePos = region.getPolePosition();
        return new LiteralMessage("x: " + polePos.x() + ", z: " + polePos.y());
    }
}