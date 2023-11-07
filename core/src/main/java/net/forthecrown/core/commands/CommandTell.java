package net.forthecrown.core.commands;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandTell extends FtcCommand {

  public CommandTell() {
    super("ftell");

    setAliases(
        "emsg", "tell", "whisper",
        "w", "msg", "etell",
        "ewhisper", "pm", "dm",
        "t", "message"
    );

    setPermission(CorePermissions.MESSAGE);
    setDescription("Sends a message to a player");

    register();
  }

  @Override
  public String getHelpListName() {
    return "tell";
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<player> <message>")
        .addInfo("Sends a <message> to <player>")
        .addInfo("Donators can use color codes and emotes");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /tell <user>
        .then(argument("user", Arguments.ONLINE_USER)

            // /tell <user> <message>
            .then(argument("message", Arguments.MESSAGE)

                .executes(c -> {
                  CommandSource source = c.getSource();
                  User user = Arguments.getUser(c, "user");
                  ViewerAwareMessage text = Arguments.getMessage(c, "message");

                  return send(source, user, text);
                })
            )
        );
  }

  public int send(CommandSource sender, User target, ViewerAwareMessage message)
      throws CommandSyntaxException
  {
    CommandSource receiver = target.getCommandSource();

    if (sender.isPlayer()) {
      User user = Users.get(sender.asPlayer());

      if (!user.equals(target)) {
        user.setLastMessage(receiver);
        target.setLastMessage(sender);
      }
    }

    target.setLastMessage(sender);

    run(sender, target.getCommandSource(), message);
    return 0;
  }

  static void run(CommandSource sender, CommandSource target, ViewerAwareMessage message) {
    ChannelledMessage channelled = ChannelledMessage.create(message)
        .setSource(sender)
        .addTarget(target)
        .setChannelName("commands/tell");

    channelled.setRenderer((viewer, baseMessage) -> {
      Component firstDisplay;
      Component secondDisplay;

      if (Audiences.equals(sender, target)) {
        firstDisplay = text("me");
        secondDisplay = text("me");
      } else if (Audiences.equals(viewer, sender)) {
        firstDisplay = text("me");
        secondDisplay = Text.sourceDisplayName(target, viewer);
      } else if (Audiences.equals(viewer, target)) {
        firstDisplay = Text.sourceDisplayName(sender, viewer);
        secondDisplay = text("me");
      } else {
        firstDisplay = Text.sourceDisplayName(sender, viewer);
        secondDisplay = Text.sourceDisplayName(target, viewer);
      }

      return Text.format("[&e{0}&r -> &e{1}&r] &f{2}", NamedTextColor.GOLD,
          firstDisplay, secondDisplay, baseMessage
      );
    });

    channelled.send();
  }
}