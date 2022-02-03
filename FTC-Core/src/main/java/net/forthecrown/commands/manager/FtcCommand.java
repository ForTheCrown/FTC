package net.forthecrown.commands.manager;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Economy;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import org.bukkit.ChatColor;
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

    private String helpListName;

    protected FtcCommand(@NotNull String name, @NotNull Plugin plugin) {
        super(name, plugin);

        // unknown command for permission message cuz you
        // don't need to know what kinds of commands we have
        setPermissionMessage(ChatColor.WHITE + "Unknown command. Type \"/help\" for help");

        setPermission(Permissions.COMMAND_PREFIX + getName());
        setDescription("An FTC command");

        FtcCommands.BY_NAME.put(getName(), this);
    }

    protected FtcCommand(String name){ this(name, Crown.inst()); }

    protected static Player getPlayerSender(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return c.getSource().asPlayer();
    }

    protected static CrownUser getUserSender(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return UserManager.getUser(c.getSource().asPlayer());
    }

    private static Economy bals = Crown.getEconomy();
    public static SuggestionProvider<CommandSource> suggestMonies(){
        return (c, b) -> {
            if(!c.getSource().isPlayer()) return Suggestions.empty();
            UUID id = getPlayerSender(c).getUniqueId();

            suggestIf(id, new BalSuggestion(bals.get(id), new LiteralMessage("Your entire balance")), b);

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

    private static void suggestIf(UUID id, int amount, SuggestionsBuilder builder){
        suggestIf(id, new BalSuggestion(amount, null), builder);
    }

    private static void suggestIf(UUID id, BalSuggestion pair, SuggestionsBuilder builder){
        int amount = pair.money();
        if(bals.has(id, amount) && (amount + "").toLowerCase().startsWith(builder.getRemaining().toLowerCase())) builder.suggest(amount, pair.message());
    }

    static record BalSuggestion(int money, Message message) {}

    public void setHelpListName(String descriptionName) {
        this.helpListName = descriptionName;
    }

    public String getHelpListName() {
        return helpListName;
    }

    public String getHelpOrNormalName(){
        return FtcUtils.isNullOrBlank(helpListName) ? getName() : getHelpListName();
    }
}
