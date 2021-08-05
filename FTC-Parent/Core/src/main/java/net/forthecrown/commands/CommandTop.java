package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.HeightMap;
import org.bukkit.Location;

public class CommandTop extends FtcCommand {
    public CommandTop(){
        super("top", ForTheCrown.inst());

        setPermission(Permissions.FTC_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    Location top = user.getLocation().toHighestLocation(HeightMap.WORLD_SURFACE);

                    user.createTeleport(() -> top, true, true, UserTeleport.Type.TELEPORT)
                            .start(true);
                    return 0;
                });
    }
}
