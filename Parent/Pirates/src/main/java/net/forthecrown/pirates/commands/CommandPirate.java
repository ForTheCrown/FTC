package net.forthecrown.pirates.commands;

import net.forthecrown.emperor.commands.manager.FtcCommand;
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
                .then(argument("reload")
                        .executes(c -> {
                            Pirates.inst.reloadConfig();
                            Pirates.getAuctionManager().reloadAuctions();
                            broadcastAdmin(c.getSource(), "Pirate config reloaded.");
                            return 0;
                        })
                )
                .then(argument("spawnShulker")
                        .executes(c -> {
                            Pirates.inst.shulker.spawn();
                            broadcastAdmin(c.getSource(), "Spawning Treasure Shulker");
                            return 0;
                        })
                );
    }
}
