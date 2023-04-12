package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.grenadier.GrenadierCommand;
import net.kyori.adventure.text.Component;

public class CommandStaffChat extends FtcCommand {

  public CommandStaffChat() {
    super("sc");

    setPermission(Permissions.STAFF_CHAT);
    setAliases("staffchat");
    setDescription("Sends a message to the staff chat");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Sends a message to the staff chat
   *
   *
   * Valid usages of command:
   * - /staffchat
   * - /sc
   *
   * Permissions used:
   * - ftc.staffchat
   *
   * Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<message>", "Sends a <message> to the staff chat");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("message", Arguments.CHAT)
        .executes(c -> {
          StaffChat.send(
              c.getSource(),
              c.getArgument("message", Component.class),
              true
          );
          return 0;
        })
    );
  }
}