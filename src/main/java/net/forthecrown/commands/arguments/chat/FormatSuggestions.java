package net.forthecrown.commands.arguments.chat;

import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Map;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class FormatSuggestions {
    static final Map<String, String> FORMAT_SUGGESTIONS = ImmutableMap.<String, String>builder()
            // Copied from LegacyComponentSerializerImpl
            .put(codeEntry('0', BLACK))
            .put(codeEntry('1', DARK_BLUE))
            .put(codeEntry('2', DARK_GREEN))
            .put(codeEntry('3', DARK_AQUA))
            .put(codeEntry('4', DARK_RED))
            .put(codeEntry('5', DARK_PURPLE))
            .put(codeEntry('6', GOLD))
            .put(codeEntry('7', GRAY))
            .put(codeEntry('8', DARK_GRAY))
            .put(codeEntry('9', BLUE))
            .put(codeEntry('a', GREEN))
            .put(codeEntry('b', AQUA))
            .put(codeEntry('c', RED))
            .put(codeEntry('d', LIGHT_PURPLE))
            .put(codeEntry('e', YELLOW))
            .put(codeEntry('f', WHITE))

            .put(codeEntry('k', TextDecoration.OBFUSCATED))
            .put(codeEntry('l', TextDecoration.BOLD))
            .put(codeEntry('m', TextDecoration.STRIKETHROUGH))
            .put(codeEntry('n', TextDecoration.UNDERLINED))
            .put(codeEntry('o', TextDecoration.ITALIC))

            .put(codeEntry('r', "reset"))

            .build();

    static final Map<String, String> HEX_2_NAME = ImmutableMap.<String, String>builder()
            // Copied from the above map lol
            .put(hexEntry(BLACK))
            .put(hexEntry(DARK_BLUE))
            .put(hexEntry(DARK_GREEN))
            .put(hexEntry(DARK_AQUA))
            .put(hexEntry(DARK_RED))
            .put(hexEntry(DARK_PURPLE))
            .put(hexEntry(GOLD))
            .put(hexEntry(GRAY))
            .put(hexEntry(DARK_GRAY))
            .put(hexEntry(BLUE))
            .put(hexEntry(GREEN))
            .put(hexEntry(AQUA))
            .put(hexEntry(RED))
            .put(hexEntry(LIGHT_PURPLE))
            .put(hexEntry(YELLOW))
            .put(hexEntry(WHITE))
            .build();

    private static Map.Entry<String, String> hexEntry(NamedTextColor color) {
        return Map.entry(color.asHexString().substring(1), color.toString());
    }

    private static Map.Entry<String, String> codeEntry(char code, Object o) {
        return Map.entry("&" + code, o.toString());
    }
}