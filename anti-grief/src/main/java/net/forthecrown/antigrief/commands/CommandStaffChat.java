package net.forthecrown.antigrief.commands;

import net.forthecrown.antigrief.GriefPermissions;
import net.forthecrown.antigrief.StaffChat;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.ViewerAwareMessage;
import org.bukkit.command.ConsoleCommandSender;

public class CommandStaffChat extends FtcCommand {

  public CommandStaffChat() {
    super("sc");

    setPermission(GriefPermissions.STAFF_CHAT);
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
          ViewerAwareMessage message = Arguments.getMessage(c, "message");

          StaffChat.newMessage()
              .setSource(c.getSource())
              .setMessage(message)
              .setLogged(!c.getSource().is(ConsoleCommandSender.class))
              .send();

          return 0;
        })
    );
  }
}