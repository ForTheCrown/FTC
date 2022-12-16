package net.forthecrown.commands.admin;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import org.bukkit.HeightMap;
import org.bukkit.Location;

public class CommandTop extends FtcCommand {
    public CommandTop(){
        super("top");

        setPermission(Permissions.ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    Location top = user.getLocation().toHighestLocation(HeightMap.WORLD_SURFACE);

                    user.createTeleport(() -> top, UserTeleport.Type.TELEPORT)
                            .setDelayed(false)
                            .setSetReturn(false)
                            .start();
                    return 0;
                });
    }
}