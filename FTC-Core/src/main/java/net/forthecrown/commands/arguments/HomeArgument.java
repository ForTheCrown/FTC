package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.forthecrown.royalgrenadier.GrenadierUtils.correctReader;

public class HomeArgument implements ArgumentType<HomeParseResult>, VanillaMappedArgument {
    private static final HomeArgument HOME = new HomeArgument();
    protected HomeArgument() {}

    public static HomeArgument home(){
        return HOME;
    }

    public static Location getHome(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        return c.getArgument(argument, HomeParseResult.class).getHome(c.getSource(), false);
    }

    public static final TranslatableExceptionType UNKNOWN_HOME = new TranslatableExceptionType("homes.noSuchHome");

    @Override
    public HomeParseResult parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        if(reader.canRead() && reader.peek() == ':'){
            reader.skip();

            String homeName = reader.readUnquotedString();

            UUID id = FtcUtils.uuidFromName(name);
            if(id == null) throw UserArgument.UNKNOWN_USER.createWithContext(correctReader(reader, cursor), Component.text(name));

            return new HomeParseResult(correctReader(reader, cursor), id, homeName);
        }

        return new HomeParseResult(correctReader(reader, cursor), name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        CommandSource source = (CommandSource) context.getSource();
        List<String> suggestions = new ArrayList<>();

        if(source.hasPermission(Permissions.HOME_OTHERS)){
            suggestions.addAll(ListUtils.convert(Bukkit.getOnlinePlayers(), plr -> plr.getName() + ':'));

            if(remaining.contains(":")){
                String name = remaining.substring(0, remaining.indexOf(':'));

                UUID id = FtcUtils.uuidFromName(name);
                if(id != null){
                    CrownUser user = UserManager.getUser(id);

                    suggestions.addAll(ListUtils.convert(user.getHomes().getHomeNames(), s -> user.getName() + ":" + s));
                }
            }
        }

        if(source.isPlayer()){
            try {
                CrownUser user = UserManager.getUser(source.asPlayer());

                suggestions.addAll(user.getHomes().getHomeNames());
            } catch (CommandSyntaxException ignored) {}
        }

        return CompletionProvider.suggestMatching(builder, suggestions);
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList("home", "BotulToxin:home", "Robinoh:nether", "base", "farm");
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.string();
    }
}