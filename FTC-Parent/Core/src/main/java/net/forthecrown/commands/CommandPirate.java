package net.forthecrown.pirates.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.pirates.Pirates;

public class CommandPirate extends FtcCommand {
    public CommandPirate() {
        super("pirate", Pirates.inst);

        setPermission("ftc.pirates.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("reload")
                        .executes(c -> {
                            Pirates.inst.reloadConfig();
                            Pirates.getAuctionManager().reloadAuctions();
                            broadcastAdmin(c.getSource(), "Pirate config reloaded.");
                            return 0;
                        })
                )

                .then(literal("spawnShulker")
                        .executes(c -> {
                            Pirates.inst.shulker.spawn();
                            broadcastAdmin(c.getSource(), "Spawning Treasure Shulker");
                            return 0;
                        })
                );
    }
}
