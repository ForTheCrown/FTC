package net.forthecrown.commands.help;

import java.util.Collection;
import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.text.writer.TextWriter;

/**
 * An entry in the help map
 */
public interface HelpEntry extends Predicate<CommandSource> {

  /**
   * Writes this entry's short text.
   * <p>
   * When multiple results are found in a help map query, this will be called
   * for each found result to write this entry into a page-based format
   *
   * @param writer The writer to write to
   * @param source The source querying the help map
   */
  void writeShort(TextWriter writer, CommandSource source);

  /**
   * Writes the full-length text representing this entry.
   * <p>
   * The opposite of {@link #writeShort(TextWriter, CommandSource)}, this is
   * intended to be called when a help map query returns only 1 results which
   * can be displayed in its entirety.
   *
   * @param writer Thr writer to write to
   * @param source The source querying the help map
   */
  void writeFull(TextWriter writer, CommandSource source);

  /**
   * Gets all keywords that represent this entry
   * @return This entry's keywords
   */
  Collection<String> getKeywords();
}