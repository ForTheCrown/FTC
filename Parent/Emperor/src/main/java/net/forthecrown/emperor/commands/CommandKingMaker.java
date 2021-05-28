package net.forthecrown.emperor.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Kingship;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class CommandKingMaker extends CrownCommandBuilder {

    public CommandKingMaker(){
        super("kingmaker", CrownCore.inst());

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
                    c.getSource().sendMessage("The Current king is " + CrownCore.getKingship().getName());
                    return 0;
                })
                .then(argument("remove")
                        .executes(c ->{
                            if(CrownCore.getKingship() == null) throw FtcExceptionProvider.create("There is already no king");

                            CrownCore.getKingship().set(null);
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
        Kingship kingship = CrownCore.getKingship();
        if(kingship.get() != null) throw FtcExceptionProvider.create("There already is a king");

        CrownUser king = UserType.getUser(c, "player");

        kingship.set(king.getUniqueId());
        kingship.setFemale(isQueen);

        String prefix = "&l[&e&lKing&r&l] &r";
        if(isQueen) prefix = "&l[&e&lQueen&r&l] &r";
        Bukkit.dispatchCommand(c.getSource().asBukkit(), "tab player " + king.getName() + " tabprefix " + prefix);

        c.getSource().sendAdmin(
                king.displayName()
                        .append(Component.text(" is the new "))
                        .append(isQueen ? Kingship.queenTitle() : Kingship.kingTitle())
                        .append(Component.text(":D"))
        );
        return 0;
    }
}
