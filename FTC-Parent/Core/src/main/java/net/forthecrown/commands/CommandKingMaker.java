package net.forthecrown.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class CommandKingMaker extends FtcCommand {

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
                .then(literal("remove")
                        .executes(c ->{
                            if(CrownCore.getKingship().getUniqueId() == null) throw FtcExceptionProvider.create("There is already no king");

                            CrownCore.getKingship().set(null);
                            c.getSource().sendMessage("King has been removed");
                            return 0;
                        })
                )
                .then(argument("player", UserType.USER)
                        .executes(c -> makeKing(c, false))

                        .then(literal("queen").executes(c -> makeKing(c, true)))
                        .then(literal("king").executes(c -> makeKing(c, false)))
                );
    }

    private int makeKing(CommandContext<CommandSource> c, boolean isQueen) throws CommandSyntaxException {
        Kingship kingship = CrownCore.getKingship();
        if(kingship.getUniqueId() != null) throw FtcExceptionProvider.create("There already is a king");

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
