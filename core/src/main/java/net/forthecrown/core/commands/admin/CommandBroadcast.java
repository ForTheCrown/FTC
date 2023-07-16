package net.forthecrown.core.commands.admin;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.ChannelledMessage;
import net.forthecrown.text.ChannelledMessage.MessageRenderer;
import net.forthecrown.text.ViewerAwareMessage;

public class CommandBroadcast extends FtcCommand {

  public CommandBroadcast() {
    super("broadcast");

    setDescription("Broadcasts a message to the entire server.");
    setAliases("announce", "bc", "ac");
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Broadcasts a message to the entire server
   *
   *
   * Valid usages of command:
   * - /broadcast
   * - /bc
   *
   * Permissions used:
   * - ftc.commands.broadcast
   *
   * Author: Wout
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<message>", "Broadcasts a <message> to the entire server");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("message", Arguments.CHAT)
            .executes(c -> {
              ViewerAwareMessage message = Arguments.getMessage(c, "message");

              ChannelledMessage channelled = ChannelledMessage.create(message)
                  .setBroadcast()
                  .renderer(MessageRenderer.FTC_PREFIX)
                  .source(c.getSource());

              channelled.send();
              return 0;
            })
        );
  }
}