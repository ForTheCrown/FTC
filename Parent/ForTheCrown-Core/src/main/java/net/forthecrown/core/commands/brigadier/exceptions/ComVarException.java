package net.forthecrown.core.commands.brigadier.exceptions;

public class ComVarException extends CrownCommandException{
    public ComVarException(String message) {
        super(message);
    }

    public ComVarException(String message, String input, int cursor) {
        super(message, input, cursor);
    }
}
