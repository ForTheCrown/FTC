package net.forthecrown.royals.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royals.dungeons.bosses.Bosses;
import net.forthecrown.royals.dungeons.bosses.mobs.DungeonBoss;

import java.util.concurrent.CompletableFuture;

public class BossArgument implements ArgumentType<DungeonBoss<?>> {
    private BossArgument() {}
    public static final BossArgument BOSS = new BossArgument();

    public static DynamicCommandExceptionType UNKNOWN_BOSS = new DynamicCommandExceptionType(o -> () -> "Unknown boss: " + o);

    public static BossArgument boss(){
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
        return CommandSource.suggestMatching(builder, Bosses.BY_NAME.keySet());
    }
}
