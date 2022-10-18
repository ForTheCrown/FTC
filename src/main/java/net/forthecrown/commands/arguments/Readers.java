package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import net.forthecrown.grenadier.CompletionProvider;

public final class Readers {
    private Readers() {}

    public static boolean startsWith(StringReader reader, String s) {
        if (!reader.canRead(s.length())) {
            return false;
        }

        return CompletionProvider.startsWith(s, reader.getRemaining());
    }

    public static void skip(StringReader reader, String s) {
        if (!startsWith(reader, s)) {
            return;
        }

        for (int i = 0; i < s.length(); i++) {
            reader.skip();
        }
    }
}