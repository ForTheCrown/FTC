package net.forthecrown.pirates.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.pirates.Pirates;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class CommandUpdateLeaderboard extends CrownCommandBuilder {
    public CommandUpdateLeaderboard(){
        super("updatelb", Pirates.plugin);
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            Pirates.plugin.updateLeaderBoard();
            c.getSource().getBukkitSender().sendMessage("Leaderboard updated");
            return 0;
        });
    }

    /*@Override
    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        Pirates.plugin.updateLeaderBoard();
        return true;
    }*/
}
