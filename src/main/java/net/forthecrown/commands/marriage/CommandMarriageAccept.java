package net.forthecrown.commands.marriage;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserInteractions;

public class CommandMarriageAccept extends FtcCommand {

    public CommandMarriageAccept() {
        super("marryaccept");

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
                .then(argument("user", Arguments.USER)
                        .executes(c -> {
                            User proposed = getUserSender(c);
                            User proposer = Arguments.getUser(c, "user");
                            UserInteractions inter = proposed.getInteractions();

                            if (inter.getLastProposal() == null
                                    || !inter.getLastProposal().equals(proposer.getUniqueId())
                            ) {
                                throw Exceptions.NO_PROPOSALS;
                            }

                            inter.setWaitingFinish(proposer.getUniqueId());
                            proposer.getInteractions().setWaitingFinish(proposed.getUniqueId());

                            inter.setLastProposal(null);

                            proposer.sendMessage(Messages.proposeAcceptTarget(proposed));
                            proposed.sendMessage(Messages.proposeAcceptSender());

                            return 0;
                        })
                );
    }
}