package net.forthecrown.core.commands;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.ChannelledMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.utils.Audiences;

public class CommandMe extends FtcCommand {

  public CommandMe() {
    super("ftc_me");

    setAliases("me");
    setPermission(Permissions.DEFAULT);
    setDescription("I have no idea what the point of this command is -Julie");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /me <action>
   *
   * Permissions used:
   * ftc.default
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<action>")
        .addInfo("Broadcasts the <action> in chat")
        .addInfo("Lets you trick people into thinking you died")
        .addInfo("by doing '/me was blown up by Creeper'");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("action", Arguments.MESSAGE)
            .executes(c -> {
              CommandSource source = c.getSource();
              ViewerAwareMessage message = Arguments.getMessage(c, "action");

              ChannelledMessage channelled = ChannelledMessage.create(message)
                  .setSource(source)
                  .setChannelName("commands/me")
                  .setBroadcast();

              channelled.setRenderer((viewer, baseMessage) -> {
                var builder = text();

                if (!Audiences.equals(viewer, source)) {
                  builder.append(text("* "), Text.sourceDisplayName(source, viewer), space());
                }

                return builder.append(baseMessage).build();
              });

              channelled.send();
              return 0;
            })
        );

    /*
    command
        .then(argument("action", StringArgumentType.greedyString())
            .executes(c -> {
              CommandSource source = c.getSource();
              boolean mayBroadcast = true;


              Component displayName = Text.sourceDisplayName(source);
              Component action = Text.renderString(
                  source.asBukkit(), c.getArgument("action", String.class)
              );

              //Check they didn't use a banned word
              if (BannedWords.checkAndWarn(source.asBukkit(), action)) {
                return 0;
              }

              Component formatted = Component.text()
                  .append(Component.text("* "))
                  .append(displayName)
                  .append(Component.space())
                  .append(action)
                  .build();

              source.sendMessage(formatted);

              if (mayBroadcast) {
                Users.getOnline()
                    .stream()
                    .filter(user -> !user.getName().equals(source.textName()))
                    .forEach(user -> user.sendMessage(formatted));
              }
              return 0;
            })
        );*/
  }
}