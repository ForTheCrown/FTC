package net.forthecrown.command.help;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.BukkitServices;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;

public interface FtcHelpList {

  static FtcHelpList helpList() {
    return BukkitServices.loadOrThrow(FtcHelpList.class);
  }

  /**
   * Queries the help map for a message to show.
   * <p>
   * The result returned by this method may either be a list of valid results,
   * or the text of a single result.
   *
   * @param tag The input data, may be null or empty
   * @param page The page the user wishes to view
   * @param pageSize The size of the page the user wishes to see
   * @param source The source querying the help map, used for
   *               permission testing
   * @return The message to display to the given user
   *
   * @throws CommandSyntaxException If the query was invalid, or if the page
   * number was invalid, relative to the amount of query results
   */
  Component query(CommandSource source, String tag, int page, int pageSize)
      throws CommandSyntaxException;

  CompletableFuture<Suggestions> suggest(CommandSource source, SuggestionsBuilder builder);

  void addEntry(HelpEntry entry);

  void addCommand(FtcCommand command);

  Collection<FtcCommand> getExistingCommands();

  Collection<HelpEntry> getAllEntries();

  Collection<HelpEntry> getEntries(String keyword);

  void update();

}