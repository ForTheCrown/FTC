package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;

import java.util.concurrent.CompletableFuture;

public class DungeonBossArgument implements ArgumentType<DungeonBoss<?>> {
    private DungeonBossArgument() {}
    public static final DungeonBossArgument BOSS = new DungeonBossArgument();

    public static DynamicCommandExceptionType UNKNOWN_BOSS = new DynamicCommandExceptionType(o -> () -> "Unknown boss: " + o);

    public static DungeonBossArgument boss(){
        return BOSS;
    }

    public static DungeonBoss<?> getBoss(CommandContext<CommandSource> c, String argument){
        return c.getArgument(argument, DungeonBoss.class);
    }

    @Override
    public DungeonBoss<?> parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        DungeonBoss result = Bosses.BY_NAME.get(name);
        if(result == null){
            reader.setCursor(cursor);
            throw UNKNOWN_BOSS.createWithContext(reader, name);
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestMatching(builder, Bosses.BY_NAME.keySet());
    }
}
