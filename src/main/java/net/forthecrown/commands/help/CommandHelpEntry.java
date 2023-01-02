package net.forthecrown.commands.help;

import static net.kyori.adventure.text.Component.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;

@Getter
@RequiredArgsConstructor
public class CommandHelpEntry implements HelpEntry {
  private final FtcCommand command;

  @Override
  public void writeShort(TextWriter writer, CommandSource source) {
    Component c = text()
        .append(
            text("/" + command.getName(), writer.getFieldStyle()),
            writer.getFieldSeparator(),
            text(command.getDescription(), writer.getFieldValueStyle())
        )
        .hoverEvent(command.asHoverEvent(source))
        .build();

    writer.write(c);
  }

  @Override
  public void writeFull(TextWriter writer, CommandSource source) {
    writer.line(Messages.PAGE_BORDER);
    writer.space();
    writer.write("/" + command.getHelpListName());
    writer.space();
    writer.write(Messages.PAGE_BORDER);

    command.writeMetadata(writer, source);
    command.writeUsages(writer, source, true);

    writer.line(Messages.PAGE_BORDER);
    writer.write(Messages.PAGE_BORDER);
    writer.write(Messages.PAGE_BORDER);
  }

  @Override
  public Collection<String> getKeywords() {
    Set<String> strings = new HashSet<>();
    strings.add(command.getHelpListName());
    strings.add(command.getName());

    if (command.getAliases() != null) {
      strings.addAll(Arrays.asList(command.getAliases()));
    }

    strings.addAll(command.createKeywords());
    return strings;
  }

  @Override
  public boolean test(CommandSource source) {
    return command.test(source);
  }

  @Override
  public String toString() {
    return "/" + command.getHelpListName();
  }
}