package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.entity.Player;

import static net.forthecrown.text.Messages.CMD_SUICIDE;

public class CommandSuicide extends FtcCommand {
    public CommandSuicide(){
        super("suicide");

        setPermission(Permissions.CMD_SUICIDE);
        setDescription("Commits suicide D:");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();

                    player.setHealth(0);
                    player.sendMessage(CMD_SUICIDE);
                    return 0;
                });
    }
}