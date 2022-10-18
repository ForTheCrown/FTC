package net.forthecrown.book.builder;

import net.forthecrown.core.Crown;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;

public final class TextInfo {
    private static final Logger LOGGER = Crown.logger();

    public static int getCharPxWidth(char c) {
        return switch(c) {
            case '!', '\'', ',', '.', ':', ';', 'i', '|' -> 1;
            case '`', 'l' -> 2;
            case '"', '(', ')', '*', 'I', '[', ']', 't', '{', '}', ' ' -> 3;
            case '<', '>', 'f', 'k' -> 4;
            case '@', '~', '✔' -> 6;
            case '✖' -> 7;
            default -> 5;
        };
    }

    public static int getPxWidth(String s) {
        return s.chars().reduce(0, (val, c) -> val + getCharPxWidth((char) c) + 1);
    }

    public static @NotNull String getFiller(@Nonnegative int pixels) {
        if (pixels < 1) {
            return "";
        }

        if (pixels == 2) {
            return ".";
        }

        if (pixels == 3) {
            return "`";
        }

        StringBuilder buffer = new StringBuilder();

        if ((pixels & 1) == 0) {
            pixels -= 3;
            buffer.append("`");
        }

        int divided = pixels / (getCharPxWidth('.') + 1);
        buffer.append(".".repeat(divided));

        return buffer.toString();
    }
}