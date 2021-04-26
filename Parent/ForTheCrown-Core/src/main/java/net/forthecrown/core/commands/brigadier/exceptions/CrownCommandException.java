package net.forthecrown.core.commands.brigadier.exceptions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.utils.CrownUtils;

/**
 * Represents the exception that can be thrown by Brigadier Commands
 * <p>Stops the code and sends the sender the given message.</p>
 * <p>The "String input, int cursor" argument will give the "&lt;- [HERE]" thing to the message</p>
 */
public class CrownCommandException extends CommandSyntaxException {
    public static final CrownExceptionType TYPE = new CrownExceptionType();

    public CrownCommandException(Message message){
        super(TYPE, message);
    }

    public CrownCommandException(String message) {
        this(new LiteralMessage(CrownUtils.translateHexCodes(message)));
    }

    public CrownCommandException(Message message, String input, int cursor){
        super(TYPE, message, input, cursor);
    }

    public CrownCommandException(String message, String input, int cursor) {
        this(new LiteralMessage(CrownUtils.translateHexCodes(message)), input, cursor);
    }
}
