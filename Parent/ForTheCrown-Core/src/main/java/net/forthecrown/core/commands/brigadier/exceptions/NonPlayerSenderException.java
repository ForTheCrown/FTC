package net.forthecrown.core.commands.brigadier.exceptions;

/**
 * Thrown when a command that shouldn't be executed by a non player is executed by a non player
 */
public class NonPlayerSenderException extends CrownCommandException{
    public NonPlayerSenderException() {
        super("Only players may execute this command");
    }
}
