package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandGems extends CrownCommandBuilder {

    public CommandGems(){
        super("gems", FtcCore.getInstance());

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
                            c.getSource().sendMessage(ComponentUtils.convertString("&e" + other.getName() + " &7has &e" + other.getGems() + " Gems"));
                            return -1000;
                        })
                )
                .executes(c ->{
                    CrownUser user = getUserSender(c);
                    user.sendMessage("&7You have &e" + user.getGems() + " Gem" + (user.getGems() == 1 ? "" : "s"));
                    return 1000;
                });
    }
}
