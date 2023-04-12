package net.forthecrown.commands.help;

import com.google.common.base.Joiner;
import java.util.function.Predicate;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.annotations.SyntaxConsumer;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class FtcSyntaxConsumer implements SyntaxConsumer {

  private static final Logger LOGGER = Loggers.getLogger();

  @Override
  public void accept(GrenadierCommandNode node,
                     Object commandObject,
                     String argument,
                     Component info,
                     @Nullable Predicate<CommandSource> condition
  ) {
    var helpMap = FtcHelpMap.getInstance();
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
    String category = commandObject.getClass().getPackageName()
        .replace("net.forthecrown.commands.", "")
        .replace("net.forthecrown.commands", "");

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
      helpMap.add(entry);
    }
  }
}