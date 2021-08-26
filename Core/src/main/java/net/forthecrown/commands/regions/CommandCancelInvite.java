package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

public class CommandCancelInvite extends FtcCommand {

    public CommandCancelInvite() {
        super("cancelinvite");

        setPermission(Permissions.REGIONS);
        setDescription("Cancels a region invite");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Cancels an invite sent to a user for a
     * region visit
     *
     * Valid usages of command:
     * /cancelinvite <user>
     *
     * Permissions used: ftc.regions
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserArgument.getUser(c, "user");

                            if(!user.getInteractions().hasInvited(target.getUniqueId())) {
                                throw FtcExceptionProvider.translatable("regions.invite.cancel.notSent", target.nickDisplayName());
                            }

                            user.getInteractions().removeInvite(target.getUniqueId());
                            target.getInteractions().removeInvitedTo(user.getUniqueId());

                            user.sendMessage(
                                    Component.translatable("regions.invite.cancel", target.nickDisplayName())
                            );
                            return 0;
                        })
                );
    }
}