package net.forthecrown.core.commands.brigadier.exceptions;

/**
 * Thrown when a player cannot afford a transaction
 */
public class CannotAffordTransactionException extends CrownCommandException {
    public CannotAffordTransactionException(){
        super("&7You cannot afford that");
    }
    public CannotAffordTransactionException(String message){
        super("&7You cannot afford that " + message);
    }
}
