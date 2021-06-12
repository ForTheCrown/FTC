package net.forthecrown.emperor.commands.marriage;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserInteractions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarriageAccept extends FtcCommand {

    public CommandMarriageAccept() {
        super("marryaccept", CrownCore.inst());

        setAliases("maccept");
        setPermission(Permissions.MARRY);
        setDescription("Accept a marriage proposal");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /marryaccept
     * /maccept
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.user())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserType.getUser(c, "user");
                            UserInteractions inter = user.getInteractions();

                            if(inter.getLastMarriageRequest() == null || !inter.getLastMarriageRequest().equals(target.getUniqueId())){
                                throw FtcExceptionProvider.translatable("marriage.noRequest", user.nickDisplayName());
                            }

                            inter.setWaitingFinish(target.getUniqueId());
                            target.getInteractions().setWaitingFinish(user.getUniqueId());

                            inter.setLastMarriageRequest(null);

                            user.sendMessage(
                                    Component.translatable("marriage.request.accepted.target",
                                            Component.text("/visit cathedral").color(NamedTextColor.YELLOW))
                                            .color(NamedTextColor.GRAY)
                            );

                            if(target.isOnline()){
                                target.sendMessage(
                                        Component.translatable("marriage.request.accepted.sender",
                                                user.nickDisplayName().color(NamedTextColor.GOLD),
                                                Component.text("/visit cathedral").color(NamedTextColor.YELLOW)
                                        ).color(NamedTextColor.GRAY)
                                );
                            }

                            return 0;
                        })
                );
    }
}