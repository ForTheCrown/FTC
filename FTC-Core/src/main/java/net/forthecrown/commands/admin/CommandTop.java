package net.forthecrown.commands.admin;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.HeightMap;
import org.bukkit.Location;

// Top me :point_right::point_left: :pleading_face:
// I'll see myself out... sorry
public class CommandTop extends FtcCommand {
    public CommandTop(){
        super("top", Crown.inst());

        setPermission(Permissions.ADMIN);
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