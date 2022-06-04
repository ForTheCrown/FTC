package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

public class RegionArgument implements ArgumentType<RegionParseResult>, VanillaMappedArgument {
    private static final RegionArgument INSTANCE = new RegionArgument();
    private RegionArgument() {}

    public static final TranslatableExceptionType UNKNOWN_REGION = new TranslatableExceptionType("regions.parse.unknown");
    public static final TranslatableExceptionType NOT_INVITED = new TranslatableExceptionType("regions.parse.notInvited");
    public static final TranslatableExceptionType NO_HOME = new TranslatableExceptionType("regions.parse.noHome");

    public static RegionArgument region() {
        return INSTANCE;
    }

    public static PopulationRegion getRegion(CommandContext<CommandSource> c, String arg, boolean checkInvite) throws CommandSyntaxException {
        return c.getArgument(arg, RegionParseResult.class).getRegion(c.getSource(), checkInvite);
    }

    public static PopulationRegion regionInviteIgnore(CommandContext<CommandSource> c, String arg) throws CommandSyntaxException {
        return getRegion(c, arg, false);
    }

    @Override
    public RegionParseResult parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        //Try getting it by name and returning that
        RegionParseResult initialResult = parseByName(reader);
        if(initialResult != null) return initialResult;

        //Try getting it from a user parse.
        try {
            UserArgument argument = UserArgument.onlineUser();
            UserParseResult result = argument.parse(reader);

            return new RegionParseResult(result);
        } catch (CommandSyntaxException e) {
            //User parse failed, it is an unknown region
            throw UNKNOWN_REGION.createWithContext(
                    GrenadierUtils.correctReader(reader, cursor),
                    Component.text(reader.getString().substring(cursor, reader.getCursor())),
                    Component.text("/listregions")
            );
        }
    }

    private RegionParseResult parseByName(StringReader origReader) {
        //Clone the reader so we could parse a user from the same point later
        //if this fails
        StringReader reader = new StringReader(origReader.getString());
        reader.setCursor(origReader.getCursor());

        //Read a name and get region from that
        String name = reader.readUnquotedString();
        PopulationRegion region = Crown.getRegionManager().get(name);

        //Region doesn't exist
        if(region == null) return null;

        //Region exists, modify original reader and return result
        origReader.setCursor(reader.getCursor());
        return new RegionParseResult(region);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        //List players and target selector
        FtcSuggestionProvider.suggestPlayerNames((CommandSource) context.getSource(), builder);

        //List regions
        String token = builder.getRemainingLowerCase();
        for (PopulationRegion r: Crown.getRegionManager().getNamedRegions()) {
            if(r.getName().toLowerCase().startsWith(token)) builder.suggest(r.getName(), r.suggestionTooltip());
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.string();
    }
}