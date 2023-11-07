package net.forthecrown.core.commands.admin;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.core.announcer.AutoAnnouncer;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;

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
              PlaceholderRenderer renderer = Placeholders.newRenderer().useDefaults();
              AutoAnnouncer announcer = CorePlugin.plugin().getAnnouncer();

              ChannelledMessage channelled = ChannelledMessage.create(message)
                  .setBroadcast()
                  .setRenderer(announcer.renderer(renderer))
                  .setSource(c.getSource());

              channelled.send();
              return 0;
            })
        );
  }
}