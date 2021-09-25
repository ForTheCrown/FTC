package net.forthecrown.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

public class CommandKingMaker extends FtcCommand {

    public CommandKingMaker(){
        super("kingmaker", Crown.inst());

        setDescription("This command is used to assign and unassign a king or queen");
        setPermission(Permissions.KING_MAKER);

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
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    c.getSource().sendMessage("The Current king is " + Crown.getKingship().getName());
                    return 0;
                })
                .then(literal("remove")
                        .executes(c ->{
                            if(Crown.getKingship().getUniqueId() == null) throw FtcExceptionProvider.create("There is already no king");

                            Crown.getKingship().set(null);
                            c.getSource().sendMessage("King has been removed");
                            return 0;
                        })
                )
                .then(argument("player", UserArgument.user())
                        .then(literal("queen").executes(c -> makeKing(c, true)))
                        .then(literal("king").executes(c -> makeKing(c, false)))
                );
    }

    private int makeKing(CommandContext<CommandSource> c, boolean isQueen) throws CommandSyntaxException {
        Kingship kingship = Crown.getKingship();
        if(kingship.getUniqueId() != null) throw FtcExceptionProvider.create("There already is a king");

        CrownUser king = UserArgument.getUser(c, "player");

        CrownUser previous = kingship.getUser();
        if(previous != null) previous.setCurrentPrefix(null);

        kingship.set(king.getUniqueId());
        kingship.setFemale(isQueen);

        Component prefix = isQueen ? Kingship.queenTitle() : Kingship.kingTitle();

        king.setCurrentPrefix(prefix);

        c.getSource().sendAdmin(
                king.displayName()
                        .append(Component.text(" is the new "))
                        .append(prefix)
                        .append(Component.text(":D"))
        );
        return 0;
    }
}
