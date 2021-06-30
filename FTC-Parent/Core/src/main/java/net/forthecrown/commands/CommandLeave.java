package net.forthecrown.commands;

import net.forthecrown.core.Permissions;
import net.forthecrown.utils.math.CrownBoundingBox;
import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.utils.Pair;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class CommandLeave extends FtcCommand {

    private final static Map<CrownBoundingBox, Pair<Location, Predicate<Player>>> ALLOWED_USAGE_AREAS = new HashMap<>();

    public CommandLeave(){
        super("leave", CrownCore.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("I'm out :D");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);
            for (Map.Entry<CrownBoundingBox, Pair<Location, Predicate<Player>>> e: ALLOWED_USAGE_AREAS.entrySet()){
                if(!e.getKey().contains(player.getLocation())) continue;

                Pair<Location, Predicate<Player>> par = e.getValue();
                if(par.getSecond().test(player)) player.teleport(par.getFirst());
                return 0;
            }
            return 0;
        });
    }

    public static void add(CrownBoundingBox box, Location exitLocation, Predicate<Player> onExit){
        ALLOWED_USAGE_AREAS.put(box, new Pair<>(exitLocation, onExit));
    }

    public static void remove(CrownBoundingBox box){
        ALLOWED_USAGE_AREAS.remove(box);
    }
}
