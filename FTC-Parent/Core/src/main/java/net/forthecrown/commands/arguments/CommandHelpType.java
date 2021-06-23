package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.royalgrenadier.GrenadierUtils;

public class CommandHelpType implements ArgumentType<Integer> {
    public static final CommandHelpType HELP_TYPE = new CommandHelpType();
    private CommandHelpType() {}

    public static int MAX;

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        int page = reader.readInt();

        if(page > 0) page--;
        if(page > MAX) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), page, MAX);

        return page;
    }
}
