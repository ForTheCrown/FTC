package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import org.bukkit.entity.Player;

public class CommandSay extends FtcCommand {

  public CommandSay() {
    super("Say");

    setPermission(Permissions.DEFAULT);
    setDescription("Says a message in chat");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /Say
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("msg", StringArgumentType.greedyString())
            .executes(c -> {
              String msg = c.getArgument("msg", String.class);

              if (c.getSource().isPlayer()) {
                Player player = c.getSource().asPlayer();
                player.chat(msg);
              } else {

              }

              return 0;
            })
        );
  }
}