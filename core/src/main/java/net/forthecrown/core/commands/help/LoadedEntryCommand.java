package net.forthecrown.core.commands.help;

import java.util.HashSet;
import java.util.Set;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.TextWriters;
import net.forthecrown.text.placeholder.Placeholders;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

public class LoadedEntryCommand extends FtcCommand {

  private final LoadedHelpEntry entry;

  public LoadedEntryCommand(String name, LoadedHelpEntry entry) {
    super(name);
    this.entry = entry;

    Set<String> labels = new HashSet<>(entry.getLabels());
    labels.remove(name);

    setAliases(labels);
    getCommand().withDescription(entry.getShortText().asComponent());
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      var writer = TextWriters.newWriter();
      writer.placeholders(Placeholders.newRenderer().useDefaults());
      writer.setFieldStyle(Style.style(NamedTextColor.YELLOW));

      entry.writeFull(writer, c.getSource());

      c.getSource().sendMessage(writer.asComponent());
      return 0;
    });
  }
}
