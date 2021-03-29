package net.forthecrown.core.commands.brigadier.exceptions;

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
