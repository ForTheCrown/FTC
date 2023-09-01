package net.forthecrown.dialogues;

import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.command.Commands;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Getter
@RequiredArgsConstructor
public class DialogueRenderer {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final EnumArgument<ButtonType> BUTTON_PARSER
      = ArgumentTypes.enumType(ButtonType.class);

  private final User user;
  private final Dialogue entry;

  public DialogueOptions getOptions() {
    return getEntry().getOptions();
  }

  public Component render(Component text) {
    PlaceholderRenderer list = Placeholders.newRenderer()
        .useDefaults()
        .add("button", (match, ctx) -> replaceButton(match));

    return list.render(text, user);
  }

  private Component replaceButton(String result) {
    if (result.isEmpty()) {
      return null;
    }

    StringReader reader = new StringReader(result);

    try {
      return parseButton(reader);
    } catch (CommandSyntaxException exc) {
      LOGGER.error("Error parsing button: {}", exc.getMessage());
      return null;
    }
  }

  private Component parseButton(StringReader reader) throws CommandSyntaxException {
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

    return type.render(this, txt, input).hoverEvent(hover);
  }

  Component readText(StringReader reader) throws CommandSyntaxException {
    if (reader.peek() == '"' || reader.peek() == '\'') {
      String quoted = reader.readQuotedString();
      return Text.valueOf(quoted, user);
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
      Component render(DialogueRenderer renderer, @NotNull Component text, @NotNull String input) {
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
      Component render(DialogueRenderer renderer, @NotNull Component text, @NotNull String input) {
        String[] split = input.split(";");

        if (split.length > 2 || split.length < 1) {
          throw new IllegalStateException(
              "Expected 'dialog' type to follow pattern: 'file;node_name'"
          );
        }

        String entryName = split[0];
        String nodeName = split.length == 2 ? split[1] : null;

        var plugin = JavaPlugin.getPlugin(DialoguesPlugin.class);
        var registry = plugin.getManager().getRegistry();
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
      Component render(DialogueRenderer renderer, @NotNull Component text, @NotNull String input) {
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
      Component render(DialogueRenderer renderer, @NotNull Component text, @NotNull String input) {
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
      Component render(DialogueRenderer renderer, @NotNull Component text, @NotNull String input) {
        return buttonize(
            text,
            true,
            openUrl(input),
            renderer.getOptions()
        );
      }
    };

    abstract Component render(DialogueRenderer renderer, Component text, String input);
  }
}