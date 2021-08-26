package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.TreasureShulker;

public class CommandPirate extends FtcCommand {
    public CommandPirate() {
        super("pirate");

        setPermission("ftc.pirates.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("reload")
                        .executes(c -> {
                            Pirates.getTreasure().reload();
                            Pirates.getParkour().reload();
                            Pirates.getAuctions().reloadAuctions();
                            c.getSource().sendAdmin( "Pirate config reloaded.");
                            return 0;
                        })
                )

                .then(literal("update_lb")
                        .executes(c -> {
                            Pirates.getLeaderboard().create();

                            c.getSource().sendAdmin("Updated pirate leaderboard");
                            return 0;
                        })
                )

                .then(literal("spawnShulker")
                        .executes(c -> {
                            Pirates.getTreasure().relocate();
                            c.getSource().sendAdmin( "Spawning Treasure Shulker");
                            return 0;
                        })
                )

                .then(literal("shulker_where")
                        .executes(c -> {
                            TreasureShulker shulker = Pirates.getTreasure();

                            c.getSource().sendMessage(FtcFormatter.clickableLocationMessage(shulker.getLocation(), true));
                            return 0;
                        })
                );
    }
}
