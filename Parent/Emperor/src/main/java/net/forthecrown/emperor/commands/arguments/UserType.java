package net.forthecrown.emperor.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.commands.manager.UserCommandExceptionType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.emperor.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.types.selector.EntityArgumentImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.ArgumentParserSelector;
import net.minecraft.server.v1_16_R3.ICompletionProvider;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserType implements ArgumentType<UserParseResult> {
    public static final UserType USER = new UserType(false, true);
    public static final UserType USERS = new UserType(true, true);
    public static final UserType ONLINE_USER = new UserType(false, false);

    public static final DynamicCommandExceptionType UNKNOWN_USER = new DynamicCommandExceptionType(o -> () -> "Unkown player: " + o);
    public static final SimpleCommandExceptionType NO_USERS_FOUND = new SimpleCommandExceptionType(() -> "No players found");
    public static final UserCommandExceptionType USER_NOT_ONLINE = new UserCommandExceptionType(
            u -> u.nickDisplayName()
                    .color(NamedTextColor.DARK_GRAY)
                    .append(Component.text(" is not online.").color(NamedTextColor.GRAY))
    );

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
        UserParseResult result = c.getArgument(argument, UserParseResult.class);
        List<CrownUser> users = result.getUsers(c.getSource());
        users.removeIf(u -> !result.checkSourceCanSee(u, c.getSource()));

        if(users.size() < 1) throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
        return users;
    }

    public static CrownUser getUser(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        UserParseResult result = c.getArgument(argument, UserParseResult.class);
        CrownUser user = result.getUser(c.getSource());
        if(!result.allowOffline() && !result.checkSourceCanSee(user, c.getSource())) throw EntityArgumentImpl.PLAYER_NOT_FOUND.create();

        return user;
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
            return new UserParseResult(selector, allowOffline);
        }

        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();
        UUID id = CrownUtils.uuidFromName(name);

        if(id == null) throw UNKNOWN_USER.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), name);

        CrownUser result = UserManager.getUser(id);
        if(!result.isOnline() && !allowOffline){
            result.unload();
            throw USER_NOT_ONLINE.create(result);
        }

        return new UserParseResult(result, allowOffline);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if(context.getSource() instanceof CommandSource){
            StringReader reader = new StringReader(builder.getInput());
            reader.setCursor(builder.getStart());

            CommandSource source = (CommandSource) context.getSource();
            ArgumentParserSelector parser = new ArgumentParserSelector(reader, true);

            try {
                parser.parse();
            } catch (CommandSyntaxException ignored) {}

            return parser.a(builder, b -> {
                Collection<String> collection = ListUtils.convert(CrownUtils.getVisiblePlayers(source), Player::getName);
                ICompletionProvider.b(collection, b);
            });

        } else return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EntityArgument.players().getExamples();
    }
}