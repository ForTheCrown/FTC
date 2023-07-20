package net.forthecrown.core.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.grenadier.GrenadierCommand;
import org.bukkit.entity.Player;

public class CommandSuicide extends FtcCommand {

  public CommandSuicide() {
    super("suicide");

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
          player.sendMessage(CoreMessages.CMD_SUICIDE);
          return 0;
        });
  }
}