package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandSudo extends FtcCommand {

  public static final String CHAT_PREFIX = "chat:";

  public CommandSudo() {
    super("sudo");

    setDescription(
        "Forces another user to perform a command or to write in chat"
    );

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory = factory.withPrefix("<user>");

    factory.usage("<command>")
        .addInfo("Forces a <user> to perform a <command>");

    factory.usage("chat:<message>")
        .addInfo("Forces a <user> to write in chat");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .then(argument("text", StringArgumentType.greedyString())
                .suggests((c, b) -> {
                  String token = b.getRemaining().toLowerCase();

                  if (token.isBlank()
                      || CHAT_PREFIX.startsWith(token.toLowerCase())
                  ) {
                    Completions.suggest(b, CHAT_PREFIX);
                  }

                  if (!token.startsWith(CHAT_PREFIX)) {
                    return FtcSuggestions.COMMAND_SUGGESTIONS
                        .getSuggestions(c, b);
                  }

                  return Suggestions.empty();
                })

                .executes(c -> {
                  CommandSource source = c.getSource();
                  User user = Arguments.getUser(c, "user");

                  String text = c.getArgument("text", String.class);
                  boolean chat = text.startsWith(CHAT_PREFIX);

                  if (chat) {
                    text = text.substring(CHAT_PREFIX.length()).trim();

                    user.getPlayer().chat(text);
                    source.sendSuccess(Messages.sudoChat(user, text));
                  } else {
                    user.getPlayer().performCommand(text);
                    source.sendSuccess(Messages.sudoCommand(user, text));
                  }

                  return 0;
                })
            )
        );
  }
}