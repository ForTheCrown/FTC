package net.forthecrown.core.commands.brigadier.exceptions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownUtils;

public class CrownCommandException extends CommandSyntaxException {
    public CrownCommandException(String message) {
        super(new CrownExceptionType(), new LiteralMessage(CrownUtils.translateHexCodes(message)));
    }

    public CrownCommandException(String message, String input, int cursor) {
        super(new CrownExceptionType(), new LiteralMessage(CrownUtils.translateHexCodes(message)), input, cursor);
    }
}
