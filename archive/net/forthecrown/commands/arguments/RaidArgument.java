package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.valhalla.Valhalla;
import net.forthecrown.valhalla.data.VikingRaid;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public class RaidArgument implements ArgumentType<VikingRaid> {
    public static final RaidArgument INSTANCE = new RaidArgument();
    private RaidArgument() {}

    public static final DynamicCommandExceptionType UNKNOWN_RAID = new DynamicCommandExceptionType(o -> () -> "Unknown raid: " + o.toString());

    public static RaidArgument raid() {
        return INSTANCE;
    }

    @Override
    public VikingRaid parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = FtcCommands.ftcKeyType().parse(reader);

        VikingRaid raid = Valhalla.getInstance().getRaid(key);
        if(raid == null) throw UNKNOWN_RAID.createWithContext(GrenadierUtils.correctReader(reader, cursor), key);

        return raid;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestKeys(builder, Valhalla.getInstance().getExistingRaids());
    }
}
