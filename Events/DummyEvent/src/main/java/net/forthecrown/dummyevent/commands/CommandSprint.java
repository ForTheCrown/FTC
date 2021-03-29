package net.forthecrown.dummyevent.commands;

import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.crownevents.entries.TimerEntry;
import net.forthecrown.dummyevent.SprintEvent;
import net.forthecrown.dummyevent.SprintMain;
import org.bukkit.entity.Player;

public class CommandSprint extends CrownCommandBuilder {

    public CommandSprint(){
        super("sprintevent", SprintMain.plugin);

        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("debug")
                        .then(argument("start")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    SprintEvent event = SprintMain.event;
                                    event.start(player);
                                    broadcastAdmin(c.getSource(), "Starting event!");
                                    return 0;
                                })
                        )
                        .then(argument("end")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    SprintEvent event = SprintMain.event;
                                    TimerEntry entry = SprintEvent.PARTICIPANTS.get(player);
                                    event.endAndRemove(entry);

                                    broadcastAdmin(c.getSource(), "Stopping event for you");
                                    return 0;
                                })
                        )
                )
                .then(argument("updatelb")
                        .executes(c -> {
                            SprintMain.leaderboard.update();
                            broadcastAdmin(c.getSource(), "Updating leaderboard");
                            return 0;
                        })
                );
    }
}
