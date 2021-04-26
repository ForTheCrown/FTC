package net.forthecrown.core.commands.brigadier.types.custom;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.ArgumentScoreholder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserType extends CrownArgType<CrownUser> {

    static final UserType USER = new UserType();

    private UserType(){
        super(obj -> new LiteralMessage("Unknown player: " + obj.toString()));
    }

    @Override
    protected CrownUser parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();
        UUID id = CrownUtils.uuidFromName(name);

        if(id == null){
            reader.setCursor(cursor);
            throw exception.createWithContext(reader, name);
        }

        return UserManager.getUser(id);
    }
    public static ArgumentScoreholder user(){
        return ArgumentScoreholder.a();
    }

    public static ArgumentScoreholder users(){
        return ArgumentScoreholder.b();
    }

    public static <S> CrownUser getUser(CommandContext<S> c, String argument, boolean allowOffline) throws CommandSyntaxException {
        String argumentActual = ArgumentScoreholder.a((CommandContext<CommandListenerWrapper>) c, argument);
        UUID id = CrownUtils.uuidFromName(argumentActual);

        if(id == null){
            StringReader reader = new StringReader(c.getInput());

            int cursor = 0;
            for (ParsedCommandNode<?> node: c.getNodes()){
                if(!node.getNode().getName().equals(argument)) continue;
                cursor = node.getRange().getStart();
            }

            reader.setCursor(cursor);

            throw USER.exception.createWithContext(reader, argumentActual);
        }

        CrownUser user = UserManager.getUser(id);
        if(!user.isOnline() && !allowOffline) throw new CrownCommandException(user.getName() + " is not online");
        return user;
    }

    public static <S> CrownUser getUser(CommandContext<S> context, String argument) throws CommandSyntaxException {
        return getUser(context, argument, true);
    }

    public static <S> CrownUser getOnlineUser(CommandContext<S> c, String argument) throws CommandSyntaxException {
        return getUser(c, argument, false);
    }

    public static <S extends CommandListenerWrapper> Collection<CrownUser> getUsers(CommandContext<S> c, String argument, boolean allowOffline) throws CommandSyntaxException {
        Collection<String> argumentActual = ArgumentScoreholder.b((CommandContext<CommandListenerWrapper>) c, argument);
        Collection<CrownUser> users = new ArrayList<>();

        for (String s: argumentActual){
            UUID id = CrownUtils.uuidFromName(s);
            if(id == null) throw USER.exception.create(s);

            CrownUser user = UserManager.getUser(id);
            if(!user.isOnline() && !allowOffline) continue;

            users.add(user);
        }
        if(users.size() < 1) throw ArgumentEntity.e.create();

        return users;
    }

    public static <S extends CommandListenerWrapper> Collection<CrownUser> getUsers(CommandContext<S> c, String argument) throws CommandSyntaxException {
        return getUsers(c, argument, true);
    }

    public static <S extends CommandListenerWrapper> Collection<CrownUser> getOnlineUsers(CommandContext<S> c, String argument) throws CommandSyntaxException {
        return getUsers(c, argument, false);
    }

    public static <S> CompletableFuture<Suggestions> suggest(CommandContext<S> c, SuggestionsBuilder b){
        return suggest(c, b, false);
    }

    public static <S> CompletableFuture<Suggestions> suggestSelector(CommandContext<S> c, SuggestionsBuilder b){
        return suggest(c, b, true);
    }

    public static <S> CompletableFuture<Suggestions> suggest(CommandContext<S> c, SuggestionsBuilder b, boolean allowSelector){
        if(allowSelector) return ArgumentEntity.c().listSuggestions(c, b);

        return CrownCommandBuilder.suggestMatching(b, ListUtils.convert(Bukkit.getOnlinePlayers(), Player::getName));
    }
}