package net.forthecrown.core.commands.brigadier.exceptions;

public class CannotAffordTransactionException extends CrownCommandException {
    public CannotAffordTransactionException(){
        super("You cannot afford that");
    }
    public CannotAffordTransactionException(String message){
        super("You cannot afford that " + message);
    }
}
