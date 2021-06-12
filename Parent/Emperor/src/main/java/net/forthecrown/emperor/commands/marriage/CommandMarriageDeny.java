package net.forthecrown.emperor.commands.marriage;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserInteractions;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarriageDeny extends FtcCommand {

    public CommandMarriageDeny() {
        super("marrydeny", CrownCore.inst());

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
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    UserInteractions inter = user.getInteractions();

                    if(inter.getLastMarriageRequest() == null) throw FtcExceptionProvider.translatable("marriage.noRequest");

                    CrownUser lastRequest = UserManager.getUser(inter.getLastMarriageRequest());
                    inter.setLastMarriageRequest(null);

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