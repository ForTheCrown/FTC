package net.forthecrown.core.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.Bukkit;

public class CommandKingMaker extends CrownCommandBuilder {

    public CommandKingMaker(){
        super("kingmaker", FtcCore.getInstance());

        setDescription("This command is used to assign and unassign a king or queen");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Makes a player a king or queen, or removes
     * the current king or queen.
     *
     * Valid usages of command:
     * - /kingmaker remove
     * - /kingmaker <player> [king | queen]
     *
     * Main Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    c.getSource().sendMessage("The Current king is " + Bukkit.getOfflinePlayer(FtcCore.getKing()).getName());
                    return 0;
                })
                .then(argument("remove")
                        .executes(c ->{
                            if(FtcCore.getKing() == null) throw FtcExceptionProvider.create("There is already no king");

                            FtcCore.setKing(null);
                            c.getSource().sendMessage("King has been removed");
                            return 0;
                        })
                )
                .then(argument("player", UserType.USER)
                        .executes(c -> makeKing(c, false))

                        .then(argument("queen").executes(c -> makeKing(c, true)))
                        .then(argument("king").executes(c -> makeKing(c, false)))
                );
    }

    private int makeKing(CommandContext<CommandSource> c, boolean isQueen) throws CommandSyntaxException {
        if(FtcCore.getKing() != null) throw FtcExceptionProvider.create("There already is a king");

        CrownUser king = UserType.getUser(c, "player");

        FtcCore.setKing(king.getUniqueId());
        c.getSource().sendMessage(king.getName() + " is now the new king :D");

        String prefix = "&l[&e&lKing&r&l] &r";
        if(isQueen) prefix = "&l[&e&lQueen&r&l] &r";
        Bukkit.dispatchCommand(c.getSource().asBukkit(), "tab player " + king.getName() + " tabprefix " + prefix);
        return 0;
    }
}
