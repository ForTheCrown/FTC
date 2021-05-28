package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserInteractions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandIgnore extends CrownCommandBuilder {
    public CommandIgnore(){
        super("ignore", CrownCore.inst());

        setPermission((String) null);
        setDescription("Makes you ignore another player");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.user())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserType.getUser(c, "user");

                            if(target.equals(user)) throw FtcExceptionProvider.create("You cannot ignore yourself");

                            UserInteractions userInt = user.getInteractions();

                            boolean alreadyIgnoring = userInt.isBlockedPlayer(target.getUniqueId());

                            if(alreadyIgnoring){
                                user.sendMessage(
                                        Component.text("No longer ignoring ")
                                                .color(NamedTextColor.YELLOW)
                                                .append(target.nickDisplayName().color(NamedTextColor.GOLD))
                                );

                                userInt.removeBlocked(target.getUniqueId());
                            } else {
                                user.sendMessage(
                                        Component.text("Ignoring ")
                                                .color(NamedTextColor.GRAY)
                                                .append(target.nickDisplayName().color(NamedTextColor.YELLOW))
                                );

                                userInt.addBlocked(target.getUniqueId());
                            }

                            return 0;
                        })
                );
    }
}
