package net.forthecrown.core.commands.brigadier.exceptions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Thrown when a command that shouldn't be executed by a non player is executed by a non player
 */
public class InvalidSenderException extends CrownCommandException{
    public InvalidSenderException() {
        this(Player.class);
    }

    public <T extends CommandSender> InvalidSenderException(Class<T> clazz){
        super("Only " + clazz.getSimpleName() + "s may execute this command");
    }
}
