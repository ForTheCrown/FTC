package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.pirates.Pirates;

public class CommandPirate extends CrownCommandBuilder {
    public CommandPirate() {
        super("pirate", Pirates.inst);

        setPermission("ftc.pirates.admin");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("reload")
                        .executes(c -> {
                            Pirates.inst.reloadConfig();
                            Pirates.inst.updateDate();
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

    /*@Override
    public boolean run(@Nonnull CommandSender sender,@Nonnull Command command,@Nonnull String label,@Nonnull String[] args) throws CrownException {
        Pirates.plugin.reloadConfig();
        Pirates.plugin.updateDate();
        Pirates.getAuctionManager().reloadAuctions();
        sender.sendMessage(ChatColor.GRAY + "Pirate config reloaded.");
        return true;
    }*/
}
