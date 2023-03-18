package net.forthecrown.utils.dialogue;

import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptManager;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class DialogueRenderer {
  public static final Pattern FUNCTION_PATTERN
      = Pattern.compile("\\\\?\\$\\{[^}]+}");

  private static final Logger LOGGER = Loggers.getLogger();

  private static final EnumArgument<ButtonType> BUTTON_PARSER
      = ArgumentTypes.enumType(ButtonType.class);

  private final User user;
  private final Dialogue entry;

  public DialogueOptions getOptions() {
    return getEntry().getOptions();
  }

  public Component render(Component text) {
    TextReplacementConfig config = TextReplacementConfig.builder()
        .match(FUNCTION_PATTERN)
        .replacement((result, builder) ->  replaceFunction(result))
        .build();

    return text.replaceText(config);
  }

  private Component replaceFunction(MatchResult result) {
    LOGGER.debug("Replacing group: {}", result.group());

    // If escaped
    if (result.group().startsWith("\\")) {
      return Component.text(result.group().substring(1));
    }

    String input = result.group()
        .substring(2);

    input = input.substring(0, input.length() - 1).trim();
    StringReader reader = new StringReader(input);

    LOGGER.debug("Trimmed function input={}", input);

    try {
      var text = parseFunction(reader);

      return text == null
          ? Component.text(result.group())
          : text;

    } catch (CommandSyntaxException exc) {
      LOGGER.error(
          "Error running replaceFunction, input='{}', reader.remaining='{}'",
          result.group(), reader.getRemaining()
      );

      throw new RuntimeException(exc);
    } catch (RuntimeException runtime) {
      LOGGER.error(
          "Error running replaceFunction, input='{}', reader.remaining='{}'",
          result.group(), reader.getRemaining()
      );

      throw runtime;
    }
  }

  private Component parseFunction(StringReader reader)
      throws CommandSyntaxException
  {
    String label = reader.readUnquotedString();
    reader.skipWhitespace();

    if (label.equals("button")) {
      return parseButton(reader);
    }

    if (label.equals("js") || label.equals("script")) {
      return parseScript(label, reader);
    }

    if (label.equals("reader")) {
      return user.displayName();
    }

    return null;
  }

  private Component parseButton(StringReader reader)
      throws CommandSyntaxException
  {
    reader.expect(':');
    reader.skipWhitespace();
    ButtonType type = BUTTON_PARSER.parse(reader);

    reader.skipWhitespace();
    reader.expect('=');
    reader.skipWhitespace();

    String input;

    if (reader.peek() == '\'' || reader.peek() == '"') {
      input = reader.readQuotedString();
    } else {
      input = Arguments.FTC_KEY.parse(reader);

      if (Readers.startsWith(reader, ";")) {
        reader.skip();
        var secondKey = Arguments.FTC_KEY.parse(reader);
        input += ";" + secondKey;
      }
    }

    reader.skipWhitespace();

    if (!reader.canRead() || !Readers.startsWith(reader, "text")) {
      throw new IllegalStateException("'text' field required");
    }

    Commands.skip(reader, "text");
    reader.expect('=');

    Component txt = readText(reader);
    HoverEvent<?> hover = null;

    if (Readers.startsWithIgnoreCase(reader, "hover")) {
      Commands.skip(reader, "hover");
      reader.expect('=');

      var text = readText(reader);
      hover = text.asHoverEvent();
    }

    return type.render(this, txt, input)
        .hoverEvent(hover);
  }

  private Component parseScript(String label, StringReader reader)
      throws CommandSyntaxException
  {
    reader.expect('=');
    Script script = readScript(label, reader);

    script.put("reader", user);
    var result = script.eval();
    script.close();

    return Text.valueOf(result.result().orElse(null));
  }

  static Component readText(StringReader reader)
      throws CommandSyntaxException
  {
    if (reader.peek() == '"' || reader.peek() == '\'') {
      String quoted = reader.readQuotedString();
      return Text.valueOf(quoted);
    }

    int c = reader.getCursor();
    String label = reader.readUnquotedString();
    reader.skipWhitespace();

    if (label.equals("JSON")) {
      return ArgumentTypes.component().parse(reader);
    }

    reader.setCursor(c);
    String remainder = reader.getRemaining();
    reader.setCursor(reader.getTotalLength());

    return Text.valueOf(remainder);
  }

  static Script readScript(String label, StringReader reader)
      throws CommandSyntaxException {
    if (label.equals("js")) {
      String jsCode = reader.readQuotedString();
      reader.skipWhitespace();

      ScriptSource source = ScriptSource.of(jsCode);
      return Script.of(source).compile();
    }

    if (!label.equals("script")) {
      throw new IllegalStateException(
          "Unknown function type: " + label
              + " should be one of 'js' or 'script'"
      );
    }

    String path = Arguments.SCRIPT.parse(reader);
    reader.skipWhitespace();

    String[] args = {};

    if (reader.peek() == ',') {
      reader.skip();
      reader.skipWhitespace();

      String argsLabel = reader.readUnquotedString();

      if (!argsLabel.equals("args")) {
        throw new IllegalStateException(
            "Expected either end of function '}' or 'args', found: "
                + argsLabel
        );
      }

      reader.skipWhitespace();
      reader.expect('=');
      reader.skipWhitespace();
      String quoted = reader.readQuotedString();
      args = quoted.split(" ");

      reader.skipWhitespace();
    }

    ScriptSource source = ScriptSource.of(
        ScriptManager.getInstance()
            .getScriptFile(path)
    );

    var script = Script.of(source);
    script.compile(args);
    return script;
  }

  public static Component buttonize(Component text,
                                    boolean available,
                                    ClickEvent clickEvent,
                                    DialogueOptions options
  ) {
    return buttonize(
        text,
        available,
        clickEvent,
        options.getButtonAvailableColor(),
        options.getButtonUnavailableColor()
    );
  }

  public static Component buttonize(@NotNull Component text,
                                    boolean available,
                                    ClickEvent clickEvent,
                                    TextColor availableColor,
                                    TextColor unavailableColor
  ) {
    Objects.requireNonNull(text);
    Objects.requireNonNull(availableColor);

    TextColor color = available
        ? availableColor
        : unavailableColor;

    var result = Text.format("[{0}]", color, text);

    if (available) {
      return result.clickEvent(clickEvent);
    } else {
      return result;
    }
  }

  public enum ButtonType {
    NODE {
      @Override
      Component render(DialogueRenderer renderer,
                       @NotNull Component text,
                       @NotNull String input
      ) {
        var tree = renderer.getEntry();
        var node = tree.getNodeByName(input);

        if (node == null) {
          return buttonize(
              Text.format("No node with name '{0}'", input),
              false,
              null,
              renderer.getOptions()
          );
        }

        return node.createButton(
            text,
            renderer.getUser(),
            renderer.getOptions()
        );
      }
    },

    DIALOG {
      @Override
      Component render(DialogueRenderer renderer,
                       @NotNull Component text,
                       @NotNull String input
      ) {
        String[] split = input.split(";");

        if (split.length > 2 || split.length < 1) {
          throw new IllegalStateException(
              "Expected 'dialog' type to follow pattern: 'file;node_name'"
          );
        }

        String entryName = split[0];
        String nodeName = split.length == 2 ? split[1] : null;

        var registry = DialogueManager.getDialogues().getRegistry();
        var entry = registry.get(entryName).orElseThrow();

        var node = nodeName == null
            ? entry.getEntryPoint()
            : entry.getNodeByName(nodeName);

        if (node == null) {
          throw new IllegalStateException(
              "No such node named '" + nodeName + "' in entry '"
                  + entryName + "'"
          );
        }

        return node.createButton(
            text,
            renderer.getUser(),
            renderer.getOptions()
        );
      }
    },

    RUN_COMMAND {
      @Override
      Component render(DialogueRenderer renderer,
                       @NotNull Component text,
                       @NotNull String input
      ) {
        return buttonize(
            text,
            true,
            runCommand(input),
            renderer.getOptions()
        );
      }
    },

    SUGGEST_COMMAND {
      @Override
      Component render(DialogueRenderer renderer,
                       @NotNull Component text,
                       @NotNull String input
      ) {
        return buttonize(
            text,
            true,
            suggestCommand(input),
            renderer.getOptions()
        );
      }
    },

    OPEN_URL {
      @Override
      Component render(DialogueRenderer renderer,
                       @NotNull Component text,
                       @NotNull String input
      ) {
        return buttonize(
            text,
            true,
            openUrl(input),
            renderer.getOptions()
        );
      }
    };

    abstract Component render(DialogueRenderer renderer,
                              Component text,
                              String input
    );
  }
}