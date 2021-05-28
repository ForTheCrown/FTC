package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandSudo extends CrownCommandBuilder {
    public CommandSudo(){
        super("sudo", CrownCore.inst());

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.onlineUser())
                        .then(argument("text", StringArgumentType.greedyString())
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
