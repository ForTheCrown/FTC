package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandGems extends FtcCommand {

    public CommandGems(){
        super("gems", Crown.inst());

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
                .then(argument("player", UserArgument.user())

                        .executes(c ->{
                            CrownUser other = UserArgument.getUser(c, "player");

                            c.getSource().sendMessage(
                                    Component.translatable("user.valueQuery.other",
                                            other.nickDisplayName().color(NamedTextColor.GOLD),
                                            FtcFormatter.queryGems(other.getGems()).color(NamedTextColor.YELLOW)
                                    ).color(NamedTextColor.GRAY)
                            );
                            return 0;
                        })
                )
                .executes(c ->{
                    CrownUser user = getUserSender(c);
                    user.sendMessage(
                            Component.translatable("user.valueQuery.self", FtcFormatter.queryGems(user.getGems()).color(NamedTextColor.YELLOW))
                                    .color(NamedTextColor.GRAY)
                    );
                    return 1000;
                });
    }
}
