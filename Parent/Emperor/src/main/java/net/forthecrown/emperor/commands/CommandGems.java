package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandGems extends FtcCommand {

    public CommandGems(){
        super("gems", CrownCore.inst());

        setDescription("Shows the amount of gems you have or another player has.");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Shows the amount of gems you or another player have
     *
     * Valid usages of command:
     * - /gems [player]
     *
     * Main Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("player", UserType.USER)

                        .executes(c ->{
                            CrownUser other = UserType.getUser(c, "player");

                            c.getSource().sendMessage(
                                    Component.translatable("user.valueQuery.other",
                                            other.nickDisplayName().color(NamedTextColor.GOLD),
                                            ChatFormatter.queryGems(other.getGems()).color(NamedTextColor.YELLOW)
                                    ).color(NamedTextColor.GRAY)
                            );
                            return 0;
                        })
                )
                .executes(c ->{
                    CrownUser user = getUserSender(c);
                    user.sendMessage(
                            Component.translatable("user.valueQuery.self", ChatFormatter.queryGems(user.getGems()).color(NamedTextColor.YELLOW))
                                    .color(NamedTextColor.GRAY)
                    );
                    return 1000;
                });
    }
}
