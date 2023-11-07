package net.forthecrown.core.placeholder;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.text.placeholder.OptionedPlaceholder;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

class ComponentPlaceholder extends OptionedPlaceholder {

  private static final ArgumentOption<String> TEXT = quotedString("content").build();

  private static final ArgumentOption<String> TRANS = quotedString("translation")
      .build();

  private static final ArgumentOption<String> SELECTOR = quotedString("selector")
      .build();

  private static final ArgumentOption<String> OBJ_NAME = quotedString("objective")
      .build();

  private static final ArgumentOption<TextColor> COLOR = Options.argument(new ColorParser())
      .setLabel("color")
      .build();

  private static final ArgumentOption<String> HOVER = quotedString("hover").build();

  private static final ArgumentOption<Boolean> ITALIC = boolOpt("italic");
  private static final ArgumentOption<Boolean> BOLD = boolOpt("bold");
  private static final ArgumentOption<Boolean> UNDERLINED = boolOpt("underlined");
  private static final ArgumentOption<Boolean> STRIKETHROUGH = boolOpt("strikethrough");
  private static final ArgumentOption<Boolean> OBFUSCATED = boolOpt("obfuscated");

  private static final ArgumentOption<String> RUN_COMMAND = quotedString("run_command").build();

  private static final ArgumentOption<String> SUGGEST_CMD = quotedString("suggest_command")
      .build();

  private static final ArgumentOption<String> OPEN_URL = quotedString("open_url")
      .build();

  private static final ArgumentOption<String> COPY_TEXT = quotedString("copy")
      .build();

  private static final OptionsArgument ARGUMENT = OptionsArgument.builder()
      .requireOneOf(TEXT, TRANS)
      .addRequired(OBJ_NAME, b -> b.exclusiveWith(TEXT, TRANS).requires(SELECTOR))
      .addOptional(SELECTOR, b -> b.requires(OBJ_NAME))

      .oneOf(COPY_TEXT, OPEN_URL, RUN_COMMAND, SUGGEST_CMD)

      .addOptional(HOVER)

      .addOptional(COLOR)
      .addOptional(ITALIC)
      .addOptional(BOLD)
      .addOptional(UNDERLINED)
      .addOptional(STRIKETHROUGH)
      .addOptional(OBFUSCATED)

      .build();

  public ComponentPlaceholder() {
    super(ARGUMENT);
  }

  private static ArgumentOption.Builder<String> quotedString(String name) {
    return Options.argument(new StringParser()).setLabel(name);
  }

  private static ArgumentOption<Boolean> boolOpt(String name) {
    return Options.argument(BoolArgumentType.bool(), name);
  }

  @Override
  public @Nullable Component render(ParsedOptions options, PlaceholderContext render) {
    Style style = loadStyle(options);

    if (options.has(TEXT)) {
      return text(options.getValue(TEXT), style);
    }

    if (options.has(TRANS)) {
      return Component.translatable(options.getValue(TRANS), style);
    }

    if (options.has(OBJ_NAME)) {
      return Component.score(options.getValue(SELECTOR), options.getValue(OBJ_NAME)).style(style);
    }

    return null;
  }

  private Style loadStyle(ParsedOptions options) {
    var builder = Style.style();

    options.getValueOptional(COLOR).ifPresent(builder::color);

    options.getValueOptional(ITALIC).ifPresent(state -> {
      builder.decoration(TextDecoration.ITALIC, state);
    });

    options.getValueOptional(BOLD).ifPresent(state -> {
      builder.decoration(TextDecoration.BOLD, state);
    });

    options.getValueOptional(UNDERLINED).ifPresent(state -> {
      builder.decoration(TextDecoration.UNDERLINED, state);
    });

    options.getValueOptional(OBFUSCATED).ifPresent(state -> {
      builder.decoration(TextDecoration.OBFUSCATED, state);
    });

    options.getValueOptional(STRIKETHROUGH).ifPresent(state -> {
      builder.decoration(TextDecoration.STRIKETHROUGH, state);
    });

    options.getValueOptional(RUN_COMMAND).ifPresent(string -> {
      builder.clickEvent(ClickEvent.runCommand(string));
    });

    options.getValueOptional(SUGGEST_CMD).ifPresent(string -> {
      builder.clickEvent(ClickEvent.suggestCommand(string));
    });

    options.getValueOptional(OPEN_URL).ifPresent(string -> {
      builder.clickEvent(ClickEvent.openUrl(string));
    });

    options.getValueOptional(COPY_TEXT).ifPresent(string -> {
      builder.clickEvent(ClickEvent.copyToClipboard(string));
    });

    options.getValueOptional(HOVER).ifPresent(string -> {
      builder.hoverEvent(text(string));
    });

    return builder.build();
  }

  static class StringParser implements ArgumentType<String> {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
      if (!reader.canRead()) {
        throw Exceptions.create("End-of-input");
      }

      String str = reader.readString();
      return str.replace("\\n", "\n");
    }
  }

  private static class ColorParser implements ArgumentType<TextColor> {

    @Override
    public TextColor parse(StringReader reader) throws CommandSyntaxException {
      String str = TEXT.getArgumentType().parse(reader);

      if (str.startsWith("0x")) {
        str = "#" + str.substring(2);
      }

      TextColor color;

      if (str.startsWith("#")) {
        color = TextColor.fromHexString(str);
      } else {
        color = NamedTextColor.NAMES.value(str);
      }

      if (color == null) {
        throw Exceptions.format("Invalid color: '{0}'", str);
      }

      return color;
    }
  }
}
