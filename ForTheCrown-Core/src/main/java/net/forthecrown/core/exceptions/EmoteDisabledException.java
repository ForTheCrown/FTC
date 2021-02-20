package net.forthecrown.core.exceptions;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class EmoteDisabledException extends CrownException{

    private final CommandSender sender;
    public EmoteDisabledException(CommandSender sender){
        this.sender = sender;
    }

    public EmoteDisabledException senderDisabled(){
        super.sendMessage(sender, ChatColor.GRAY + "You have emotes turned off.");
        super.sendMessage(sender, ChatColor.GRAY + "Do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
        return this;
    }

    public EmoteDisabledException targetDisabled(){
        super.sendMessage(sender, ChatColor.GRAY + "This player has disabled emotes.");
        return this;
    }
}
