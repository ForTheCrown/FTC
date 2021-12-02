package net.forthecrown.commands.marriage;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarriageDeny extends FtcCommand {

    public CommandMarriageDeny() {
        super("marrydeny", Crown.inst());

        setDescription("Deny a person's marriage request");
        setAliases("mdeny");
        setPermission(Permissions.MARRY);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /marriagecancel
     *
     * Permissions used:
     * ftc.marry
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    UserInteractions inter = user.getInteractions();

                    if(inter.getLastProposal() == null) throw FtcExceptionProvider.translatable("marriage.noRequest");

                    CrownUser lastRequest = UserManager.getUser(inter.getLastProposal());
                    inter.setLastProposal(null);

                    if(lastRequest.isOnline()){
                        lastRequest.sendMessage(
                                Component.translatable("marriage.request.denied.sender",
                                        user.nickDisplayName().color(NamedTextColor.YELLOW)
                                ).color(NamedTextColor.GRAY)
                        );
                    }

                    user.sendMessage(
                            Component.translatable("marriage.request.denied.target",
                                    lastRequest.nickDisplayName().color(NamedTextColor.YELLOW)
                            ).color(NamedTextColor.GRAY)
                    );
                    return 0;
                });
    }
}