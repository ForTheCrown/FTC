package net.forthecrown.core.commands.brigadier.exceptions;

import net.md_5.bungee.api.ChatColor;

/**
 * Internal exception for the emote command
 */
public class EmoteDisabledException extends CrownCommandException {

    private static final String SENDER_DISABLED = ChatColor.GRAY + "You have emotes turned off." + "\n" + ChatColor.GRAY + "Do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.";
    private static final String TARGET_DISABLED = ChatColor.GRAY + "This player has disabled emotes.";

    private EmoteDisabledException(String message){
        super(message);
    }

    public static EmoteDisabledException senderDisabled(){
        return new EmoteDisabledException(SENDER_DISABLED);
    }

    public static EmoteDisabledException targetDisabled(){
        return new EmoteDisabledException(TARGET_DISABLED);
    }
}
