package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.chat.MessageArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.DirectMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

public class CommandTell extends FtcCommand {

  public CommandTell() {
    super("ftell");

    setAliases(
        "emsg", "tell", "whisper",
        "w", "msg", "etell",
        "ewhisper", "pm", "dm",
        "t", "message"
    );

    setPermission(Permissions.MESSAGE);
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
                  var text = c.getArgument("message", MessageArgument.Result.class);

                  return sendMsg(source, user, text);
                })
            )
        );
  }

  public int sendMsg(CommandSource sender, User target, MessageArgument.Result message)
      throws CommandSyntaxException {
    CommandSource receiver = target.getCommandSource();

    if (sender.isPlayer()) {
      User user = Users.get(sender.asPlayer());

      if (!user.equals(target)) {
        user.setLastMessage(receiver);
        target.setLastMessage(sender);
      }
    }

    target.setLastMessage(sender);

    DirectMessage.run(sender, receiver, false, message.getText());
    return 0;
  }
}