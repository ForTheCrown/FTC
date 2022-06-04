package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserCache;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserArgument implements ArgumentType<UserParseResult>, VanillaMappedArgument {
    private static final UserArgument
            USER        = new UserArgument(false, true),
            USERS       = new UserArgument(true, true),
            ONLINE_USER = new UserArgument(false, false);

    public static final TranslatableExceptionType
            UNKNOWN_USER    = new TranslatableExceptionType("user.parse.unknown"),
            NO_USERS_FOUND  = new TranslatableExceptionType("user.parse.nonFound"),
            USER_NOT_ONLINE = new TranslatableExceptionType("user.parse.notOnline");

    public static UserArgument user() {
        return USER;
    }

    public static UserArgument onlineUser() {
        return ONLINE_USER;
    }

    public static UserArgument users() {
        return USERS;
    }

    public static List<CrownUser> getUsers(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        UserParseResult result = c.getArgument(argument, UserParseResult.class);
        return result.getUsers(c.getSource(), true);
    }

    public static CrownUser getUser(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        UserParseResult result = c.getArgument(argument, UserParseResult.class);
        return result.getUser(c.getSource(), true);
    }

    public final boolean allowMultiple;
    public final boolean allowOffline;
    public UserArgument(boolean allowMultiple, boolean allowOffline){
        this.allowMultiple = allowMultiple;
        this.allowOffline = allowOffline;
    }

    @Override
    public UserParseResult parse(StringReader reader) throws CommandSyntaxException {
        if(reader.peek() == '@'){
            EntitySelector selector = allowMultiple ? EntityArgument.players().parse(reader, true) : EntityArgument.player().parse(reader, true);
            return new UserParseResult(selector, allowOffline);
        }

        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();
        UserCache.CacheEntry entry = Crown.getUserManager().getCache().get(name);

        if(entry == null) throw UNKNOWN_USER.createWithContext(GrenadierUtils.correctReader(reader, cursor), Component.text(name));

        CrownUser result = UserManager.getUser(entry.getUniqueId());
        if(!result.isOnline() && !allowOffline) {
            result.unload();
            throw USER_NOT_ONLINE.create(result.nickDisplayName());
        }

        return new UserParseResult(result, allowOffline);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if(context.getSource() instanceof CommandSource){
            StringReader reader = new StringReader(builder.getInput());
            reader.setCursor(builder.getStart());

            EntitySelectorParser parser = new EntitySelectorParser(reader, true);

            try {
                parser.parse();
            } catch (CommandSyntaxException ignored) {}

            return parser.fillSuggestions(builder, builder1 -> FtcSuggestionProvider.suggestPlayerNames((CommandSource) context.getSource(), builder1));
        } else return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EntityArgument.players().getExamples();
    }

    public ArgumentType<?> getVanillaArgumentType() {
        /*if (allowMultiple) return ScoreHolderArgument.scoreHolders();
        return ScoreHolderArgument.scoreHolder();*/

        if (allowOffline) {
            if (allowMultiple) return ScoreHolderArgument.scoreHolders();
            return ScoreHolderArgument.scoreHolder();
        }

        if (allowMultiple) return net.minecraft.commands.arguments.EntityArgument.players();
        return net.minecraft.commands.arguments.EntityArgument.player();
    }
}