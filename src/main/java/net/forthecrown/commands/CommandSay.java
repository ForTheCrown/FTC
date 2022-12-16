package net.forthecrown.commands;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandSay extends FtcCommand {

    public CommandSay() {
        super("Say");

        setPermission(Permissions.DEFAULT);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Say
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("msg", Arguments.MESSAGE)
                        .executes(c -> {
                            Component message = Arguments.getMessage(c, "msg");

                            if (c.getSource().isPlayer()) {
                                Player player = c.getSource().asPlayer();
                                Set<Audience> audiences = new ObjectArraySet<>();
                                Bukkit.getServer().audiences()
                                        .forEach(audiences::add);

                                AsyncChatEvent event = new AsyncChatEvent(
                                        false,
                                        player,
                                        audiences,
                                        ChatRenderer.defaultRenderer(),
                                        message,
                                        message
                                );

                                event.callEvent();
                            } else {
                                Component formatted = Messages.chatMessage(
                                        c.getSource().displayName(),
                                        message
                                );

                                Bukkit.getServer().sendMessage(formatted);
                            }

                            return 0;
                        })
                );
    }
}