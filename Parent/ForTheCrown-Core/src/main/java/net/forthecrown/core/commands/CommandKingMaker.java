package net.forthecrown.core.commands;

import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;

import java.util.UUID;

public class CommandKingMaker extends CrownCommandBuilder {

    public CommandKingMaker(){
        super("kingmaker", FtcCore.getInstance());

        setDescription("This command is used to assign and unassign a king or queen");
        setUsage("&7Usage:&r /kingmaker <remove | player> [king | queen]");
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
    protected void registerCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    c.getSource().getBukkitSender().sendMessage("The Current king is " + Bukkit.getOfflinePlayer(FtcCore.getKing()).getName());
                    return 0;
                })
                .then(argument("remove")
                        .executes(c ->{
                            if(FtcCore.getKing() == null) throw new CrownCommandException("There is already no king");

                            FtcCore.setKing(null);
                            c.getSource().getBukkitSender().sendMessage("King has been removed");
                            return 0;
                        })
                )
                .then(argument("player", UserType.user())
                        .suggests((c, b) -> UserType.listSuggestions(b))
                        .executes(c -> makeKing(c, false))

                        .then(argument("queen").executes(c -> makeKing(c, true)))
                        .then(argument("king").executes(c -> makeKing(c, false)))
                );
    }

    private int makeKing(CommandContext<CommandListenerWrapper> c, boolean isQueen) throws CrownCommandException {
        if(FtcCore.getKing() != null) throw new CrownCommandException("There already is a king");

        String playerName = c.getArgument("player", String.class);
        UUID id = getUUID(playerName);

        FtcCore.setKing(id);
        c.getSource().getBukkitSender().sendMessage(playerName + " is now the new king :D");

        String prefix = "&l[&e&lKing&r&l] &r";
        if(isQueen) prefix = "&l[&e&lQueen&r&l] &r";
        Bukkit.dispatchCommand(c.getSource().getBukkitSender(), "tab player " + playerName + " tabprefix " + prefix);
        return 0;
    }
}
