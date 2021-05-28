package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandBack extends CrownCommandBuilder {
    public CommandBack(){
        super("back", CrownCore.inst());

        setPermission(Permissions.BACK);
        setAliases("return");
        setDescription("Teleports you to your previous location");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    if(!user.checkTeleporting()) return 0;
                    if(user.getLastLocation() == null) throw FtcExceptionProvider.noReturnLoc();

                    user.createTeleport(user::getLastLocation, true, UserTeleport.Type.BACK).start(true);
                    return 0;
                });
    }
}
