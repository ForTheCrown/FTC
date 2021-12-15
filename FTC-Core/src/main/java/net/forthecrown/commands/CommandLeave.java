package net.forthecrown.commands;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.function.Predicate;

public class CommandLeave extends FtcCommand {
    private static final Set<LeaveBox> SET = new ObjectOpenHashSet<>();

    public CommandLeave(){
        super("leave", Crown.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("I'm out :D");
        setAliases("exit");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);

            for (LeaveBox b: SET) {
                if(b.apply(player)) return 0;
            }

            throw FtcExceptionProvider.translatable("commands.leave.cannotUseHere");
        });
    }

    public static void add(FtcBoundingBox box, Location exitLocation, Predicate<Player> onExit){
        SET.add(new LeaveBox(exitLocation, box, onExit));
    }

    private record LeaveBox(Location exit, FtcBoundingBox area, Predicate<Player> playerPredicate) {
        boolean apply(Player player) {
            if(!area.contains(player)) return false;
            if(!playerPredicate.test(player)) return false;

            player.teleport(exit);
            return true;
        }
    }
}
