package net.forthecrown.command.help;

import static net.forthecrown.command.help.AbstractHelpEntry.packageNameToCategory;

import com.google.common.base.Joiner;
import java.util.function.Predicate;
import net.forthecrown.Loggers;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.annotations.SyntaxConsumer;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FtcSyntaxConsumer implements SyntaxConsumer {

  private static final Logger LOGGER = Loggers.getLogger();

  @Override
  public void accept(GrenadierCommandNode node,
                     Object command,
                     String argument,
                     Component info,
                     @Nullable Predicate<CommandSource> condition
  ) {
    var helpMap = FtcHelpList.helpList();
    var entries = helpMap.getEntries(node.getLiteral());

    if (entries.size() > 1) {
      LOGGER.warn(
          "Cannot add usage info to command '{}' found more than "
              + "1 help entry matching the label",

          node.getLiteral()
      );

      return;
    }

    AnnotatedHelpEntry entry;
    boolean addAfter;

    if (entries.isEmpty()) {
      entry = new AnnotatedHelpEntry(node);
      addAfter = true;
    } else {
      var helpEntry = entries.iterator().next();

      if (!(helpEntry instanceof AnnotatedHelpEntry annotated)) {
        LOGGER.warn(
            "Cannot add usage info to command '{}' existing help entry is "
                + "not an annotated command entry",

            node.getLiteral()
        );

        return;
      }

      entry = annotated;
      addAfter = false;
    }

    String[] args = argument.split("\\s+");
    entry.setLabel(args[0]);
    String category = packageNameToCategory(command.getClass().getPackageName());

    entry.setCategory(category);

    args = ArrayUtils.remove(args, 0);
    String newArgument = Joiner.on(' ').join(args);

    Usage usage = new Usage(newArgument);
    String plain = Text.plain(info);
    String[] split = plain.split("\\n");

    if (condition != null) {
      usage.setCondition(condition);
    }

    for (String s : split) {
      usage.addInfo(s);
    }

    entry.getUsages().add(usage);

    if (addAfter) {
      helpMap.addEntry(entry);
    }
  }
}