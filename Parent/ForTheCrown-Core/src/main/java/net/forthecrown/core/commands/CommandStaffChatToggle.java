package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.StaffChat;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandStaffChatToggle extends CrownCommandBuilder {

    public CommandStaffChatToggle(){
        super("staffchattoggle", FtcCore.getInstance());

        setAliases("sct", "sctoggle");
        setPermission("ftc.staffchat");
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
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);

            Set<Player> set = StaffChat.sctPlayers;
            String staffPrefix = ChatColor.DARK_GRAY + "[Staff] ";

            if(set.contains(player)){
                set.remove(player);
                player.sendMessage(staffPrefix + ChatColor.GRAY + "Your messages will no longer all go to staffchat");
            } else{
                set.add(player);
                player.sendMessage(staffPrefix + ChatColor.GRAY + "Your messages will now all go to staffchat");
            }

            return 0;
        });
    }
}
