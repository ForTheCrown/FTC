package net.forthecrown.core.chat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Emotes implements SuggestionProvider<CommandSource> {

    private final Map<String, String> emoteMap = new HashMap<>();

    public void registerEmotes(){
        register("shrug", "¯\\\\_(ツ)_/¯");
        register("ughcry", "(ಥ﹏ಥ)");
        register("hug", "༼ つ ◕_◕ ༽つ");
        register("hugcry", "༼ つ ಥ_ಥ ༽つ");
        register("bear", "ʕ• ᴥ •ʔ");
        register("smooch", "( ^ 3^) ❤");
        register("why", "ლ(ಠ益ಠლ)");
        register("tableflip", "(ノಠ益ಠ)ノ彡┻━┻");
        register("tableput", " ┬──┬ ノ( ゜-゜ノ)");
        register("pretty", "(◕‿◕ ✿)");
        register("sparkle", "(ﾉ◕ヮ◕)ﾉ*･ﾟ✧");
        register("blush", "(▰˘◡˘▰)");
        register("sad", "(._. )");
        register("pleased", "(ᵔᴥᵔ)");
        register("fedup", "(¬_¬)");
        register("reallysad", "(◉︵◉ )");
    }

    private void register(String key, String emote){
        emoteMap.put(':' + key + ':', emote);
    }

    public String format(String input, @Nullable CommandSender source, boolean ignorePerms){
        if(ignorePerms || source == null || source.hasPermission(Permissions.DONATOR_3)){
            for (Map.Entry<String, String> e: emoteMap.entrySet()) {
                input = input.replaceAll(e.getKey(), e.getValue());
            }
        }

        return input;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        return getSuggestions(context, builder, false);
    }

    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder, boolean ignorePerms) {
        if(context.getSource().hasPermission(Permissions.DONATOR_3) || ignorePerms){
            return CompletionProvider.suggestMatching(builder, emoteMap.keySet());
        }

        return Suggestions.empty();
    }
}
