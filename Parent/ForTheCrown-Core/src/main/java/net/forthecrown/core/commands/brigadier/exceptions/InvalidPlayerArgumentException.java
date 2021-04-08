package net.forthecrown.core.commands.brigadier.exceptions;

/**
 * Used basically only by UserType lol
 * <p>Thrown when the inputted user in the argument doesn't exist</p>
 */
public class InvalidPlayerArgumentException extends CrownCommandException{

    public InvalidPlayerArgumentException(){
        super("&7No player found");
    }

    public InvalidPlayerArgumentException(String message) {
        super("&8" + message + " &7is not a valid player");
    }

    public InvalidPlayerArgumentException(String message, String input, int cursor) {
        super("&8" + message + " &7is not a valid player", input, cursor);
    }
}
