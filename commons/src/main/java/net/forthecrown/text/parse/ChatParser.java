package net.forthecrown.text.parse;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.Getter;
import net.forthecrown.text.ChatEmotes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;

public class ChatParser {

  private static ChatParser parsers;

  @Getter
  private final List<TextFunction> functions = new ArrayList<>();

  /**
   * A regex pattern for all color codes including hex codes
   * <p>
   * This is used by {@link #replaceColorCodes(String)} to replace all ampersand
   * color codes with section codes
   */
  private static final Pattern COLOR_CHAR_PATTERN
      = Pattern.compile("(\\\\|)&((#[0-9a-fA-F]{6})|([0-9a-fA-FK-Ok-orRxX]))");

  private static final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
      .character(SECTION_CHAR)
      .hexColors()
      .build();

  private ChatParser() {
    // Create default functions
    functions.add(new GradientFunction());
    functions.add(new PlayerFunction());
    functions.add(new TimeFunction());
    functions.add(new HyperlinkFunction());
    functions.add(new LinkFunction());
  }

  public static ChatParser parsers() {
    return parsers == null ? (parsers = new ChatParser()) : parsers;
  }

  public Component parse(String string, TextContext context) {
    if (!context.has(ChatParseFlag.IGNORE_CASE)) {
      string = checkCase(string);
    }

    Component text;

    if (context.has(ChatParseFlag.COLORS)) {
      text = serializer.deserialize(replaceColorCodes(string));
    } else {
      text = Component.text(string);
    }

    if (context.has(ChatParseFlag.EMOJIS)) {
      text = ChatEmotes.format(text);
    }

    if (functions.isEmpty()) {
      return text;
    }

    return runFunctions(text, context);
  }

  private Component runFunctions(Component text, TextContext context) {
    Component result = text;

    for (var func: functions) {
      if (!func.test(context)) {
        continue;
      }

      Pattern pattern = func.getEscapablePattern();

      TextReplacementConfig config = TextReplacementConfig.builder()
          .match(pattern)
          .replacement((result1, builder) -> {
            if (result1.group().startsWith("\\")) {
              return Component.text(result1.group().substring(1));
            }

            Component funcResult = func.format(result1, context);

            if (funcResult == null) {
              return Component.text(result1.group());
            }

            return funcResult;
          })
          .build();

      result = result.replaceText(config);
    }

    return result;
  }

  private static String checkCase(String s) {
    if (s.length() <= 8) {
      return s;
    }

    int upperCaseCount = 0;
    int half = s.length() / 2;

    for (int i = 0; i < s.length(); i++) {
      if (Character.isUpperCase(s.charAt(i))) {
        ++upperCaseCount;
      }

      // More than half the characters are uppercase
      // return filtered input
      if (upperCaseCount > half) {
        return StringUtils.capitalize(s.toLowerCase()) + "!!";
      }
    }

    return s;
  }

  /**
   * Replaces color codes in the given string
   */
  public static String replaceColorCodes(String s) {
    return COLOR_CHAR_PATTERN
        .matcher(s)
        .replaceAll(ChatParser::replaceCode);
  }

  /**
   * Replaces the first character of the given result's group with a section
   * character
   */
  private static String replaceCode(MatchResult result) {
    var group = result.group();

    if (group.startsWith("\\")) {
      return group;
    }

    return SECTION_CHAR + group.substring(1);
  }
}