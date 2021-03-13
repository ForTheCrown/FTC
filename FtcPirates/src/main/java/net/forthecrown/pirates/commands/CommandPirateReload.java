package net.forthecrown.pirates.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.pirates.Pirates;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class CommandPirateReload extends CrownCommandBuilder {
    public CommandPirateReload() {
        super("rlpirate", Pirates.plugin);

        setPermission("ftc.pirates.admin");
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            Pirates.plugin.reloadConfig();
            Pirates.plugin.updateDate();
            Pirates.getAuctionManager().reloadAuctions();
            broadcastAdmin(c.getSource(), "Pirate config reloaded.");
            return 0;
        });
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
