package net.forthecrown.core.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.grenadier.GrenadierCommand;
import org.bukkit.GameMode;
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
          var gamemode = player.getGameMode();

          if (gamemode == GameMode.SURVIVAL || gamemode == GameMode.ADVENTURE) {
            player.damage(1000, player);
          } else {
            player.setHealth(0);
          }

          player.sendMessage(CoreMessages.CMD_SUICIDE);
          return 0;
        });
  }
}