package net.forthecrown.book.builder;

public final class TextInfo {
    static int getPxLength(char c) {
        return switch (c) {
            case 'i', ':' -> 1;
            case 'l' -> 2;
            case '*', 't', '[', ']' -> 3;
            case 'f', 'k' -> 4;
            default -> 5;
        };
    }

    static int getPxLength(String string) {
        return string.chars().reduce(0, (p, i) -> p + getPxLength((char) i) + 1);
    }
}
