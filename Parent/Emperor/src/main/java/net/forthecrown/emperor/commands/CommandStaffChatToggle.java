package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.admin.StaffChat;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandStaffChatToggle extends CrownCommandBuilder {

    public CommandStaffChatToggle(){
        super("staffchattoggle", CrownCore.inst());

        setAliases("sct", "sctoggle");
        setPermission(Permissions.STAFF_CHAT);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Toggles a players every message going to staffchat
     *
     *
     * Valid usages of command:
     * - /staffchattoggle
     * - /sct
     *
     * Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();

                    Set<Player> set = StaffChat.toggledPlayers;
                    String staffPrefix = ChatColor.DARK_GRAY + "[Staff] ";

                    if(set.contains(player)){
                        set.remove(player);
                        player.sendMessage(staffPrefix + ChatColor.GRAY + "Your messages will no longer all go to StaffChat");
                    } else{
                        set.add(player);
                        player.sendMessage(staffPrefix + ChatColor.GRAY + "Your messages will now all go to StaffChat");
                    }

                    return 0;
                })

                .then(argument("visible")
                        .executes(c -> {
                            Player player = c.getSource().asPlayer();
                            Set<Player> set = StaffChat.ignoring;

                            if(set.contains(player)){
                                set.remove(player);
                                player.sendMessage(Component.text("You will no longer see StaffChat").color(NamedTextColor.GRAY));
                            } else {
                                set.add(player);
                                player.sendMessage(Component.text("You will now see StaffChat").color(NamedTextColor.GRAY));
                            }

                            return 0;
                        })

                        .then(argument("user", UserType.onlineUser())
                                .executes(c -> {
                                    CommandSource source = c.getSource();
                                    Player player = UserType.getUser(c, "user").getPlayer();
                                    Set<Player> set = StaffChat.ignoring;

                                    if(set.contains(player)){
                                        set.remove(player);
                                        player.sendMessage(Component.text("You will no longer see StaffChat").color(NamedTextColor.GRAY));
                                        source.sendMessage(Component.text(player.getName() + " will no longer see StaffChat").color(NamedTextColor.GRAY));
                                    } else {
                                        set.add(player);
                                        player.sendMessage(Component.text("You will now see StaffChat").color(NamedTextColor.GRAY));
                                        source.sendMessage(Component.text(player.getName() + " will now see StaffChat").color(NamedTextColor.GRAY));
                                    }

                                    return 0;
                                })
                        )
                );
    }
}