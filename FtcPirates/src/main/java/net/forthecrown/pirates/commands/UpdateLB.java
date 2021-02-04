package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.pirates.Pirates;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class UpdateLB extends CrownCommand {
    public UpdateLB(){
        super("updatelb", Pirates.plugin);
        register();
    }

    @Override
    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        Pirates.plugin.updateLeaderBoard();
        return true;
    }
}
