package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandLeave extends FtcCommand {
    public static final List<LeaveListener> LISTENERS = new ObjectArrayList<>();

    public CommandLeave(){
        super("leave");

        setPermission(Permissions.DEFAULT);
        setDescription("I'm out :D");
        setAliases("exit");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = c.getSource().asPlayer();

            for (LeaveListener l: LISTENERS) {
                if (l.onCall(player)) {
                    return 0;
                }
            }

            throw Exceptions.CANNOT_USE_LEAVE;
        });
    }

    public static void addListener(LeaveListener listener) {
        LISTENERS.add(listener);
    }

    public interface LeaveListener {
        /**
         * Calls the listener
         * @param player The player executing the command
         * @return True, if the listener call was successful, false otherwise
         * @throws CommandSyntaxException Thrown to indicate the command cannot be used in its current context,
         *                                will prevent any other listeners from being called
         */
        boolean onCall(Player player) throws CommandSyntaxException;
    }
}