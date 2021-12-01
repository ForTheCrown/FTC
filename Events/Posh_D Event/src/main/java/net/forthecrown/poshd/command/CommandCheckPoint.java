package net.forthecrown.poshd.command;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.forthecrown.crown.EventTimer;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.poshd.Main;
import net.forthecrown.poshd.Messages;
import org.bukkit.entity.Player;

public class CommandCheckPoint extends AbstractCommand {

    public CommandCheckPoint() {
        super("CheckPoint", Main.inst);

        setAliases("cp");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /checkpoint
     * /cp
     *
     * Permissions used:
     * NONE
     *
     * Main Author: Julie
     */

    private static final SimpleCommandExceptionType NOT_IN_EVENT = new SimpleCommandExceptionType(() -> "You are not in a parkour course");

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();
                    EventTimer timer = Main.TIMERS.get(player.getUniqueId());

                    if(timer == null) {
                        throw NOT_IN_EVENT.create();
                    }

                    timer.getOnTimerExpire().accept(player);

                    player.sendMessage(Messages.timerStart());
                    return 0;
                });
    }
}