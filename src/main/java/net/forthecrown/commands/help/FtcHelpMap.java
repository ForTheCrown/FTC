package net.forthecrown.commands.help;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcCommand.Usage;
import net.forthecrown.commands.manager.FtcCommand.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.forthecrown.utils.text.format.page.Footer;
import net.forthecrown.utils.text.format.page.Header;
import net.forthecrown.utils.text.format.page.PageEntry;
import net.forthecrown.utils.text.format.page.PageEntryIterator;
import net.forthecrown.utils.text.format.page.PageFormat;
import net.forthecrown.utils.text.writer.LoreWriter;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class FtcHelpMap {

  @Getter
  private static final FtcHelpMap instance = new FtcHelpMap();

  /** Map of all keywords, mapped to their bound values */
  private final Map<String, Collection<HelpEntry>> keywordLookup
      = new Object2ObjectOpenHashMap<>();

  /** Map of all existing commands, mapped to their name */
  private final Map<String, FtcCommand> existingCommands = new HashMap<>();

  private final LinkedList<HelpEntry> entries = new LinkedList<>();

  // Context used to pass data onto the page formatter
  private final ContextSet contextSet = ContextSet.create();
  private final ContextOption<CommandSource> sourceOption = contextSet.newOption();
  private final ContextOption<String> inputOption = contextSet.newOption();
  private final ContextOption<Integer> actualPageSize = contextSet.newOption(5);

  // Page format used to format entries for list-based display
  private final PageFormat<HelpEntry> pageFormat = PageFormat.create();

  private final PageFormat<Component> singleEntryPaginator
      = PageFormat.create();

  @SuppressWarnings({"unchecked", "rawtypes"})
  private FtcHelpMap() {
    // Initialize the page format used to display help entries

    // Footer format
    var footer = Footer.create()
        .setPageButton((viewerPage, pageSize, context) -> {
          var s = context.get(inputOption);

          return ClickEvent.runCommand(
              String.format("/help '%s' %s %s",
                  s == null ? "" : s,
                  viewerPage, context.get(actualPageSize)
              )
          );
        });

    // Header format
    Header header = Header.create();
    header.title((it, writer, context) -> {
      var s = context.get(inputOption);

      if (Strings.isNullOrEmpty(s)) {
        writer.write("Help");
      } else {
        writer.formatted("Results for: {0}", s);
      }
    });

    // Entry format
    PageEntry<HelpEntry> entry = PageEntry.create();
    entry.setEntryDisplay((writer, entry1, viewerIndex, context, it) -> {
      entry1.writeShort(writer, context.getOrThrow(sourceOption));
    });

    PageEntry<Component> singletonEntry = PageEntry.create();
    singletonEntry.setIndex((viewerIndex, entry1, it) -> null);
    singletonEntry.setEntryDisplay((writer, entry1, viewerIndex, context, it) -> {
      writer.line(entry1);
    });

    singleEntryPaginator.setFooter(footer);
    singleEntryPaginator.setHeader(header);
    singleEntryPaginator.setEntry(singletonEntry);
    pageFormat.setHeader(header);
    pageFormat.setFooter(footer);
    pageFormat.setEntry(entry);
  }

  /** Suggests help entry keywords */
  public CompletableFuture<Suggestions> suggest(CommandSource source,
                                                SuggestionsBuilder builder
  ) {
    var input = builder.getRemainingLowerCase();
    boolean beginsWithQuote
        = input.length() > 0
        && StringReader.isQuotedStringStart(input.charAt(0));

    char quote;

    if (beginsWithQuote) {
      quote = input.charAt(0);
    } else {
      quote = '"';
    }

    String unquoted = input.replaceAll(quote + "", "");

    getAll().stream()
        .filter(entry -> entry.test(source))
        .flatMap(entry -> entry.getKeywords().stream())
        .filter(s -> CompletionProvider.startsWith(unquoted, s))
        .map(s -> Commands.optionallyQuote(quote + "", s))
        .forEach(builder::suggest);

    return builder.buildFuture();
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
  public Component query(String tag,
                         int page,
                         int pageSize,
                         CommandSource source
  ) throws CommandSyntaxException {
    List<HelpEntry> entries = new LinkedList<>();

    if (Strings.isNullOrEmpty(tag) || tag.equalsIgnoreCase("all")) {
      entries.addAll(getAll());

      // Remove the ones the source doesn't have permission to see
      entries.removeIf(entry -> !entry.test(source));
    } else {
      entries.addAll(lookup(normalize(tag), source));
    }

    TextWriter writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.YELLOW));

    Context context = contextSet.createContext()
        .set(sourceOption, source)
        .set(inputOption, tag)
        .set(actualPageSize, pageSize);

    // Single entry, write that 1 entry
    if (entries.size() == 1) {
      pageSize += 5;
      var entry = entries.iterator().next();
      LoreWriter loreWriter = TextWriters.loreWriter();

      loreWriter.setFieldStyle(Style.style(NamedTextColor.YELLOW));
      entry.writeFull(loreWriter, source);

      var text = loreWriter.getLore();

      // Ensure list isn't empty and page number is valid
      Commands.ensurePageValid(page, pageSize, text.size());

      var it = PageEntryIterator.of(text, page, pageSize);
      singleEntryPaginator.write(it, writer, context);

      return writer.asComponent();
    }

    // Ensure list isn't empty and page number is valid
    Commands.ensurePageValid(page, pageSize, entries.size());

    // Format all results onto a page
    var iterator = PageEntryIterator.of(entries, page, pageSize);

    pageFormat.write(iterator, writer, context);
    return writer.asComponent();
  }

  private Collection<HelpEntry> lookup(String tag, CommandSource source) {
    // Try just calling the keyword lookup
    Collection<HelpEntry> result = keywordLookup.getOrDefault(
        tag, Collections.emptyList()
    );

    result.removeIf(entry -> !entry.test(source));

    if (!result.isEmpty()) {
      return result;
    }

    // Keyword lookup failed, loop through all keywords to find the ones
    // that match the most
    result = new ObjectArrayList<>();
    int max = tag.length() / 5 + 3;
    LevenshteinDistance distance = new LevenshteinDistance(max);

    for (var v: getAll()) {
      if (!v.test(source)) {
        continue;
      }

      var keywords = v.getKeywords();

      for (var keyword: keywords) {
        var s = normalize(keyword);
        int dis = distance.apply(tag, s);

        // -1 means above max threshold
        if (dis == -1) {
          continue;
        }

        // I STG If this condition ever returns true, I will throw
        // someone or something out of a window
        if (dis == 0) {
          result = new ObjectArrayList<>();
          result.add(v);
          return result;
        }

        result.add(v);
        break;
      }
    }

    return result;
  }

  private Collection<HelpEntry> getAll() {
    return entries;
  }

  public Collection<FtcCommand> getExistingCommands() {
    return Collections.unmodifiableCollection(existingCommands.values());
  }

  public void addCommand(FtcCommand command) {
    existingCommands.put(command.getName(), command);
  }

  /**
   * When commands are created, their aliases, permissions and so forth, get
   * set after the {@link FtcCommand} constructor is called, so this method MUST
   * be called after ALL commands have been created and registered for it to
   * properly index every keyword, alias and command label.
   */
  public void update() {
    existingCommands.forEach((s, command) -> {
      UsageFactory factory = arguments -> {
        Usage usage = new Usage(arguments);
        command.getUsages().add(usage);
        return usage;
      };
      command.populateUsages(factory);

      add(new CommandHelpEntry(command));
    });
  }

  public void add(HelpEntry entry) {
    entry.getKeywords().forEach(s -> {
      Collection<HelpEntry> entries = keywordLookup.computeIfAbsent(
          normalize(s), s1 -> new ObjectArrayList<>()
      );

      entries.add(entry);
    });

    entries.add(entry);
  }

  private static String normalize(String s) {
    if (s.startsWith("/")) {
      s = s.substring(1);
    }

    return s.toLowerCase()
        .trim()
        .replaceAll(" ", "_");
  }
}