package net.forthecrown.core.commands.admin;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandFile;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

@CommandFile("commands/timefields.gcn")
public class CommandTimeFields {

  @VariableInitializer
  void initVars(Map<String, Object> vars) {
    vars.put("field", new RegistryArguments<>(TimeField.REGISTRY, "Time field"));
    vars.put("time", LongArgumentType.longArg());
  }

  void queryFields(
      CommandSource source,
      @Argument("user") User user,
      @Argument(value = "field", optional = true) TimeField field
  ) throws CommandSyntaxException {
    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    writer.formatted("{0, user} time fields:", NamedTextColor.YELLOW, user);

    if (field == null) {
      for (TimeField value : TimeField.REGISTRY.values()) {
        if (!value.isSerialized()) {
          continue;
        }

        writeField(writer, user, value);
      }
    } else {
      writeField(writer, user, field);
    }

    source.sendMessage(writer.asComponent());
  }

  void writeField(TextWriter writer, User user, TimeField field) {
    long timestamp = user.getTime(field);

    if (timestamp == -1) {
      writer.field(field.getKey(), "unset");
    } else {
      writer.field(field.getKey(), Text.formatDate(timestamp));
    }
  }

  void setField(
      CommandSource source,
      @Argument("user") User user,
      @Argument("field") TimeField field,
      @Argument(value = "value", optional = true) Long value
  ) throws CommandSyntaxException {
    long time;
    long present = System.currentTimeMillis();

    if (value == null) {
      time = present;
    } else {
      time = value;
    }

    user.setTime(field, time);

    Component message;

    if (time == -1) {
      message = Text.format("Unset field &e{0}&r for &6{1, user}&r.",
          NamedTextColor.GRAY, field.getKey(), user
      );
    } else {
      Component timeText = time == present ? text("Present") : Text.formatDate(time);
      message = Text.format("Set field &e{0}&r to &6{1}&r for &e{2, user}&r.",
          NamedTextColor.GRAY, field.getKey(), timeText, user
      );
    }

    source.sendSuccess(message);
  }

  void unsetField(
      CommandSource source,
      @Argument("user") User user,
      @Argument("field") TimeField field
  ) throws CommandSyntaxException {
    setField(source, user, field, -1L);
  }
}
