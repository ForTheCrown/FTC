package net.forthecrown.command.help;

import com.mojang.datafixers.util.Either;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommandNode;

/**
 * Help entry for annotation-based commands
 */
public class AnnotatedHelpEntry extends AbstractHelpEntry {

  private final GrenadierCommandNode node;

  @Setter
  @Getter
  private String label;

  @Getter
  @Setter
  private String category = "";

  public AnnotatedHelpEntry(GrenadierCommandNode node) {
    this.node = node;
  }

  @Override
  public String getCategory() {
    return category;
  }

  @Override
  public CommandDisplayInfo createDisplay() {
    return CommandDisplayInfo.create(
        Either.right(node),
        getUsages(),
        label,
        category
    );
  }

  @Override
  public boolean test(CommandSource source) {
    return node.canUse(source);
  }
}