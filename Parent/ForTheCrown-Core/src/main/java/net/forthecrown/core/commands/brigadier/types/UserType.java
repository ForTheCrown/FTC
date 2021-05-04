package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserType implements ArgumentType<UserParseResult> {
    public static final UserType USER = new UserType(false, true);
    public static final UserType USERS = new UserType(true, true);
    public static final UserType ONLINE_USER = new UserType(false, false);

    public static final DynamicCommandExceptionType UNKNOWN_USER = new DynamicCommandExceptionType(o -> () -> "Unkown player: " + o);
    public static final SimpleCommandExceptionType NO_USERS_FOUND = new SimpleCommandExceptionType(() -> "No players found");
    public static final DynamicCommandExceptionType USER_NOT_ONLINE = new DynamicCommandExceptionType(o -> () -> o.toString() + " isn't online");

    public static UserType user(){
        return USER;
    }

    public static UserType onlineUser(){
        return ONLINE_USER;
    }

    public static UserType users(){
        return USERS;
    }

    public static List<CrownUser> getUsers(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        return c.getArgument(argument, UserParseResult.class).getUsers(c.getSource());
    }

    public static CrownUser getUser(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        return c.getArgument(argument, UserParseResult.class).getUser(c.getSource());
    }

    private final boolean allowMultiple;
    private final boolean allowOffline;
    public UserType(boolean allowMultiple, boolean allowOffline){
        this.allowMultiple = allowMultiple;
        this.allowOffline = allowOffline;
    }

    @Override
    public UserParseResult parse(StringReader reader) throws CommandSyntaxException {
        if(reader.peek() == '@'){
            EntitySelector selector = allowMultiple ? EntityArgument.players().parse(reader, true) : EntityArgument.player().parse(reader, true);
            return new UserParseResult(selector);
        }

        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();
        UUID id = CrownUtils.uuidFromName(name);

        if(id == null){
            reader.setCursor(cursor);
            throw UNKNOWN_USER.createWithContext(reader, name);
        }

        CrownUser result = UserManager.getUser(id);
        if(!result.isOnline() && !allowOffline) throw USER_NOT_ONLINE.create(result.getName());

        return new UserParseResult(result);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return EntityArgument.players().listSuggestions(context, builder);
    }
}
