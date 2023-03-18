package net.forthecrown.commands;

import static net.forthecrown.core.Messages.CMD_SUICIDE;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import org.bukkit.entity.Player;

public class CommandSuicide extends FtcCommand {

  public CommandSuicide() {
    super("suicide");

    setPermission(Permissions.CMD_SUICIDE);
    setDescription("Commits suicide D:");

    simpleUsages();
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          Player player = c.getSource().asPlayer();

          player.setHealth(0);
          player.sendMessage(CMD_SUICIDE);
          return 0;
        });
  }
}