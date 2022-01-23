package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.guilds.TradeGuild;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;

public class CommandVoteHistory extends FtcCommand {

    public CommandVoteHistory() {
        super("VoteHistory");

        setPermission(Permissions.GUILD);
        setDescription("Shows the history of voting in the guild");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /VoteHistory
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> showHistory(c.getSource(), 1))

                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(c -> showHistory(c.getSource(), c.getArgument("page", Integer.class)))
                );
    }

    private int showHistory(CommandSource source, int page) throws RoyalCommandException {
        TradeGuild guild = Crown.getGuild();
        page--;

        if(guild.getHistory().isEmpty()) {
            throw FtcExceptionProvider.translatable("guilds.history.noHistory");
        }

        source.sendMessage(guild.displayHistory(page));
        return 0;
    }
}