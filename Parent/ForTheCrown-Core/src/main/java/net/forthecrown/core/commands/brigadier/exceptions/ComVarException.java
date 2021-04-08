package net.forthecrown.core.commands.brigadier.exceptions;

/**
 * Thrown by the ComVarType class, most likely as a failure of string parsing
 */
public class ComVarException extends CrownCommandException{
    public ComVarException(String message) {
        super(message);
    }

    public ComVarException(String message, String input, int cursor) {
        super(message, input, cursor);
    }
}
