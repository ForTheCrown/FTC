package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class CommandSudo extends FtcCommand {
    public CommandSudo(){
        super("sudo", ForTheCrown.inst());

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.onlineUser())
                        .then(argument("text", StringArgumentType.greedyString())
                                .suggests((c, b) -> {
                                    String token = b.getRemaining().toLowerCase();
                                    if(token.isBlank() || token.startsWith("c")) CompletionProvider.suggestMatching(b, "c:");

                                    if(!token.startsWith("c:")){
                                        return CompletionProvider.suggestMatching(b, Bukkit.getCommandMap().getKnownCommands().keySet());
                                    }

                                    return Suggestions.empty();
                                })

                                .executes(c -> {
                                    CommandSource source = c.getSource();
                                    CrownUser user = UserType.getUser(c, "user");

                                    String text = c.getArgument("text", String.class);
                                    boolean chat = text.startsWith("c:");

                                    if(chat){
                                        text = text.substring(2).trim();
                                        user.getPlayer().chat(text);

                                        source.sendAdmin(
                                                Component.text("Making ")
                                                        .color(NamedTextColor.GRAY)
                                                        .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                                                        .append(Component.text(" say: "))
                                                        .append(Component.text(text).color(NamedTextColor.GOLD))
                                        );
                                        return 0;
                                    }

                                    user.getPlayer().performCommand(text);

                                    source.sendAdmin(
                                            Component.text("Making ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                                                    .append(Component.text(" run: "))
                                                    .append(Component.text(text).color(NamedTextColor.GOLD))
                                    );
                                    return 0;
                                })
                        )
                );
    }
}
