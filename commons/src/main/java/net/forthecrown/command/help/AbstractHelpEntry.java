package net.forthecrown.command.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public abstract class AbstractHelpEntry implements HelpEntry {

  @Getter
  private final List<Usage> usages = new ArrayList<>();

  public abstract CommandDisplayInfo createDisplay();

  @Override
  public String getMainLabel() {
    return createDisplay().label();
  }

  @Override
  public void writeShort(TextWriter writer, CommandSource source) {
    var info = createDisplay();
    var desc = info.description();
    HoverEvent<Component> hover = info.asHover(source);

    writer.formatted("/{0}", writer.getFieldStyle().hoverEvent(hover), info.label());

    if (desc != null) {
      writer.write(writer.getFieldSeparator().hoverEvent(hover));
      writer.write(desc.style(writer.getFieldValueStyle().hoverEvent(hover)));
    }
  }

  @Override
  public void writeFull(TextWriter writer, CommandSource source) {
    var info = createDisplay();

    info.writeMetadata(writer, source);
    info.writeUsages(writer, source, true);
  }

  @Override
  public Collection<String> getKeywords() {
    List<String> strings = new ArrayList<>();
    var info = createDisplay();

    strings.add(info.label());
    strings.addAll(info.aliases());

    return strings;
  }

  static String packageNameToCategory(String packageName) {
    return packageName
        .replace("net.forthecrown.", "")
        .replace("net.forthecrown", "")
        .replace("commands.", "")
        .replace("commands", "")
        .replace('.', '/');
  }
}