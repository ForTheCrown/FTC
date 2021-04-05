package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.exceptions.InvalidPlayerArgumentException;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserType {

    public static ArgumentEntity onlinePlayer(){
        return TargetSelectorType.player();
    }

    public static StringArgumentType user(){
        return StringArgumentType.word();
    }

    public static CrownUser getUser(CommandContext<CommandListenerWrapper> c, String argument) throws CrownCommandException {
        String id = c.getArgument(argument, String.class);
        UUID uuid = CrownUtils.uuidFromName(id);
        if(uuid == null) throw new InvalidPlayerArgumentException(id);

        return UserManager.getUser(uuid);
    }

    public static CrownUser getOnlineUser(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return UserManager.getUser(TargetSelectorType.getPlayer(c, argument));
    }

    public static CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder b){
        return CrownCommandBuilder.suggestMatching(b, ListUtils.convert(Bukkit.getOnlinePlayers(), Player::getName));
    }

    //This takes like a solid second to load all the names lol
    public static CompletableFuture<Suggestions> listALLplayers(SuggestionsBuilder b){
        String token = b.getRemaining().toLowerCase();
        for (OfflinePlayer p: Bukkit.getOfflinePlayers()){
            if(p.getName().toLowerCase().startsWith(token)) b.suggest(p.getName());
        }
        return b.buildFuture();
    }
}
