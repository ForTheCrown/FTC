package net.forthecrown.core.commands.brigadier.exceptions;

public class NonPlayerSenderException extends CrownCommandException{
    public NonPlayerSenderException() {
        super("Only players may execute this command");
    }
}
