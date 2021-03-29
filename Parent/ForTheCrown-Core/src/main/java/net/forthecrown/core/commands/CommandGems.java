package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.UserType;

public class CommandGems extends CrownCommandBuilder {

    public CommandGems(){
        super("gems", FtcCore.getInstance());

        setUsage("&7Usage: &r/gems <player>");
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
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("player", UserType.user())
                        .suggests((c, b) -> UserType.listSuggestions(b))

                        .executes(c ->{
                            CrownUser user = getUserSender(c);
                            CrownUser other = UserType.getUser(c, "player");

                            user.sendMessage("&e" + other.getName() + " &7has &e" + other.getGems() + " Gems");
                            return -1000;
                        })
                )
                .executes(c ->{
                    CrownUser user = getUserSender(c);
                    user.sendMessage("&7You have &e" + user.getGems() + " Gems");
                    return 1000;
                });
    }
}
