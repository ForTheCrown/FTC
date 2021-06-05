package net.forthecrown.emperor.commands.manager;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.economy.Balances;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.royalgrenadier.source.CommandSources;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The class used to create, build and register commands
 * <p>
 * stuff like usage and descriptions are basically worthless and exist
 * because I can't be arsed to remove them from commands that already have them
 * </p>
 */
public abstract class FtcCommand extends AbstractCommand {
    protected FtcCommand(@NotNull String name, @NotNull Plugin plugin) {
        super(name, plugin);

        permissionMessage = ChatColor.WHITE + "Unknown command. Type \"/help\" for help";
        permission = Permissions.register(Permissions.COMMAND_PREFIX + name);
        description = "An FTC command";

        CoreCommands.BY_NAME.put(name, this);
    }

    @Override
    public boolean test(CommandSource source) {
        if(CrownCore.getPunishmentManager().checkJailed(source.asBukkit())) return false;

        return testPermissionSilent(source.asBukkit());
    }

    protected CommandSender getSender(CommandContext<CommandSource> c){
        return c.getSource().asBukkit();
    }

    protected Player getPlayerSender(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return c.getSource().asPlayer();
    }

    protected CrownUser getUserSender(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return UserManager.getUser(getPlayerSender(c));
    }

    protected static void broadcastAdmin(CommandSource source, String message){
        source.sendAdmin(message);
    }

    protected void broadcastAdmin(CommandSender sender, String message){
        broadcastAdmin(CommandSources.getOrCreate(sender, this), message);
    }

    protected void broadcastAdmin(CommandContext<CommandSource> c, String message){
        broadcastAdmin(c.getSource(), message);
    }

    private static Balances bals = CrownCore.getBalances();
    protected SuggestionProvider<CommandSource> suggestMonies(){
        return (c, b) -> {
            if(!c.getSource().isPlayer()) return Suggestions.empty();
            UUID id = getPlayerSender(c).getUniqueId();

            if(bals.canAfford(id, 1)) b.suggest(bals.get(id), new LiteralMessage("Your entire balance"));

            suggestIf(id, 1, b);
            suggestIf(id, 10, b);
            suggestIf(id, 100, b);
            suggestIf(id, 1000, b);

            suggestIf(id, 5, b);
            suggestIf(id, 50, b);
            suggestIf(id, 500, b);
            suggestIf(id, 5000, b);

            return b.buildFuture();
        };
    }

    private void suggestIf(UUID id, int amount, SuggestionsBuilder builder){
        if(bals.canAfford(id, amount) && (amount + "").toLowerCase().startsWith(builder.getRemaining().toLowerCase())) builder.suggest(amount);
    }
}
