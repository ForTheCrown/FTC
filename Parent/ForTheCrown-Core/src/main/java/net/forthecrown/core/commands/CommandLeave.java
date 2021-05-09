package net.forthecrown.core.commands;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.utils.Pair;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandLeave extends CrownCommandBuilder {

    private final static Map<CrownBoundingBox, Pair<Location, Function<Player, Boolean>>> ALLOWED_USAGE_AREAS = new HashMap<>();

    public CommandLeave(){
        super("leave", FtcCore.getInstance());

        setPermission(null);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);
            for (Map.Entry<CrownBoundingBox, Pair<Location, Function<Player, Boolean>>> e: ALLOWED_USAGE_AREAS.entrySet()){
                if(!e.getKey().contains(player.getLocation())) continue;

                Pair<Location, Function<Player, Boolean>> par = e.getValue();
                if(par.getSecond().apply(player)) player.teleport(par.getFirst());
                return 0;
            }
            return 0;
        });
    }

    public static void add(CrownBoundingBox box, Location exitLocation, Function<Player, Boolean> onExit){
        ALLOWED_USAGE_AREAS.put(box, new Pair<>(exitLocation, onExit));
    }

    public static void remove(CrownBoundingBox box){
        ALLOWED_USAGE_AREAS.remove(box);
    }
}
