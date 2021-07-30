package net.forthecrown.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

public class CommandKingMaker extends FtcCommand {

    public CommandKingMaker(){
        super("kingmaker", ForTheCrown.inst());

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
     * Main Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    c.getSource().sendMessage("The Current king is " + ForTheCrown.getKingship().getName());
                    return 0;
                })
                .then(literal("remove")
                        .executes(c ->{
                            if(ForTheCrown.getKingship().getUniqueId() == null) throw FtcExceptionProvider.create("There is already no king");

                            ForTheCrown.getKingship().set(null);
                            c.getSource().sendMessage("King has been removed");
                            return 0;
                        })
                )
                .then(argument("player", UserType.USER)
                        .then(literal("queen").executes(c -> makeKing(c, true)))
                        .then(literal("king").executes(c -> makeKing(c, false)))
                );
    }

    private int makeKing(CommandContext<CommandSource> c, boolean isQueen) throws CommandSyntaxException {
        Kingship kingship = ForTheCrown.getKingship();
        if(kingship.getUniqueId() != null) throw FtcExceptionProvider.create("There already is a king");

        CrownUser king = UserType.getUser(c, "player");

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
