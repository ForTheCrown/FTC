package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.pirates.Pirates;

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
                            Pirates.getParkour().getData().reload();
                            Pirates.getAuctions().reloadAuctions();
                            broadcastAdmin(c.getSource(), "Pirate config reloaded.");
                            return 0;
                        })
                )

                .then(literal("spawnShulker")
                        .executes(c -> {
                            Pirates.getTreasure().relocate();
                            broadcastAdmin(c.getSource(), "Spawning Treasure Shulker");
                            return 0;
                        })
                );
    }
}
