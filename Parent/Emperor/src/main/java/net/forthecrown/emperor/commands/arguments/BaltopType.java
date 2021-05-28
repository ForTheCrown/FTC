package net.forthecrown.emperor.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.royalgrenadier.GrenadierUtils;

public class BaltopType implements ArgumentType<Integer> {
    public static BaltopType BALTOP = new BaltopType();
    private BaltopType() {}

    public static final int MAX = Math.round(((float) CrownCore.getBalances().getMap().size())/10);

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        int read = reader.readInt();

        if(read > 0) read--;
        if(read > MAX) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), read, MAX);

        return read;
    }
}
