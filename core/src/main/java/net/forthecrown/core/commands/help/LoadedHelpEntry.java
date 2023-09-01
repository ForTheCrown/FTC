package net.forthecrown.core.commands.help;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.HelpEntry;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.ViewerAwareMessage;

@Getter @Setter
public class LoadedHelpEntry implements HelpEntry {

  private final Set<String> labels;
  private final String mainLabel;

  private final ViewerAwareMessage shortText;
  private final ViewerAwareMessage fullText;

  private FtcCommand command;

  public LoadedHelpEntry(
      Set<String> labels,
      String label,
      ViewerAwareMessage shortText,
      ViewerAwareMessage fullText
  ) {
    this.mainLabel = label;
    this.labels = Collections.unmodifiableSet(labels);

    this.shortText = shortText;
    this.fullText = fullText;
  }

  @Override
  public void writeShort(TextWriter writer, CommandSource source) {
    writer.fieldSameLine(mainLabel, shortText);
  }

  @Override
  public void writeFull(TextWriter writer, CommandSource source) {
    writer.write(fullText);
  }

  @Override
  public Collection<String> getKeywords() {
    return labels;
  }

  @Override
  public boolean test(CommandSource source) {
    return true;
  }
}
