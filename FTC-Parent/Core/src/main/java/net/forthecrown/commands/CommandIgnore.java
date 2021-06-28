package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandIgnore extends FtcCommand {
    public CommandIgnore(){
        super("ignore", CrownCore.inst());

        setPermission(Permissions.IGNORE);
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

                            if(target.equals(user)) throw FtcExceptionProvider.cannotIgnoreSelf();

                            UserInteractions userInt = user.getInteractions();

                            boolean alreadyIgnoring = userInt.isBlockedPlayer(target.getUniqueId());

                            if(alreadyIgnoring){
                                user.sendMessage(
                                        Component.translatable("user.ignore.remove", target.nickDisplayName().color(NamedTextColor.GOLD))
                                                .color(NamedTextColor.YELLOW)
                                );

                                userInt.removeBlocked(target.getUniqueId());
                            } else {
                                user.sendMessage(
                                        Component.translatable("user.ignore.add", target.nickDisplayName().color(NamedTextColor.YELLOW))
                                                .color(NamedTextColor.GRAY)
                                );

                                userInt.addBlocked(target.getUniqueId());
                            }

                            return 0;
                        })
                );
    }
}
