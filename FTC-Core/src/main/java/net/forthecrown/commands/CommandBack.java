package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandBack extends FtcCommand {
    public CommandBack(){
        super("back", Crown.inst());

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

                    if(!user.hasPermission(Permissions.WORLD_BYPASS) && CommandTpask.isInvalidWorld(user.getLastLocation().getWorld())){
                        throw FtcExceptionProvider.cannotReturn();
                    }

                    user.createTeleport(user::getLastLocation, true, UserTeleport.Type.BACK).start(true);
                    return 0;
                });
    }
}
