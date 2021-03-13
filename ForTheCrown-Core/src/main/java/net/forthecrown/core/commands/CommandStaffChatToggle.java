package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
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
     * Permissions used:
     * - ftc.staffchat
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);

            Set<Player> set = FtcCore.getSCTPlayers();
            String staffPrefix = ChatColor.DARK_GRAY + "[Staff] ";

            if(set.contains(player)){
                set.remove(player);
                player.sendMessage(staffPrefix + ChatColor.GRAY + "Your messages will no longer all go to staffchat");
            } else{
                set.add(player);
                player.sendMessage(staffPrefix + ChatColor.GRAY + "Your messages will now all go to staffchat");
            }

            FtcCore.setSCTPlayers(set);
            return 0;
        });
    }
}
