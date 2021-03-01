package net.forthecrown.core.commands.brigadier.exceptions;

public class InvalidPlayerArgumentException extends CrownCommandException{

    public InvalidPlayerArgumentException(){
        super("Invalid player in argument");
    }

    public InvalidPlayerArgumentException(String message) {
        super(message + " is not a valid player");
    }

    public InvalidPlayerArgumentException(String message, String input, int cursor) {
        super(message + " is not a valid player", input, cursor);
    }
}
