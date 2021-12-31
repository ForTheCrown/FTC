package net.forthecrown.poshd.command;

import net.forthecrown.crown.EventTimer;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.poshd.Main;
import net.forthecrown.poshd.Messages;
import org.bukkit.entity.Player;

public class CommandLeaveParkour extends AbstractCommand {
    public CommandLeaveParkour() {
        super("leaveparkour", Main.inst);

        setAliases("pleave", "leavep", "parkourleave");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = c.getSource().asPlayer();
            EventTimer timer = Main.TIMERS.get(player.getUniqueId());

            if(timer == null) {
                throw CommandCheckPoint.NOT_IN_EVENT.create();
            }

            if(!timer.wasStopped()) timer.stop();

            player.teleport(timer.exitLocation);
            player.sendMessage(Messages.leftEvent());
            return 0;
        });
    }
}
