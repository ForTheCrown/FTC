package net.forthecrown.commands.marriage;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarriageAccept extends FtcCommand {

    public CommandMarriageAccept() {
        super("marryaccept", Crown.inst());

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
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserArgument.getUser(c, "user");
                            UserInteractions inter = user.getInteractions();

                            if(inter.getLastProposal() == null || !inter.getLastProposal().equals(target.getUniqueId())){
                                throw FtcExceptionProvider.translatable("marriage.noRequest", user.nickDisplayName());
                            }

                            inter.setWaitingFinish(target.getUniqueId());
                            target.getInteractions().setWaitingFinish(user.getUniqueId());

                            inter.setLastProposal(null);

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