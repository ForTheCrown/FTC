package net.forthecrown.core.commands;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CommandLeave extends CrownCommandBuilder {

    private final static Map<CrownBoundingBox, Location> ALLOWED_USAGE_AREAS = new HashMap<>();

    public CommandLeave(){
        super("leave", FtcCore.getInstance());

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);
            for (CrownBoundingBox b: ALLOWED_USAGE_AREAS.keySet()){
                if(!b.contains(player.getLocation())) continue;

                player.teleport(ALLOWED_USAGE_AREAS.get(b));
            }
            return 0;
        });
    }

    public static void addAllowedArea(CrownBoundingBox box, Location exitLocation){
        ALLOWED_USAGE_AREAS.put(box, exitLocation);
    }

    public static void removeAllowedArea(CrownBoundingBox box){
        ALLOWED_USAGE_AREAS.remove(box);
    }
}
