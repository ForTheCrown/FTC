package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.core.Messages;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;

public class CommandSudo extends FtcCommand {
    public static final String CHAT_PREFIX = "chat:";

    public CommandSudo(){
        super("sudo");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.ONLINE_USER)
                        .then(argument("text", StringArgumentType.greedyString())
                                .suggests((c, b) -> {
                                    String token = b.getRemaining().toLowerCase();

                                    if (token.isBlank() || token.startsWith(CHAT_PREFIX)) {
                                        CompletionProvider.suggestMatching(b, CHAT_PREFIX);
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
                                        source.sendAdmin(Messages.sudoChat(user, text));
                                    } else {
                                        user.getPlayer().performCommand(text);
                                        source.sendAdmin(Messages.sudoCommand(user, text));
                                    }

                                    return 0;
                                })
                        )
                );
    }
}