package net.forthecrown.core.commands.help;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.Loggers;
import net.forthecrown.command.Commands;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.CommandHelpEntry;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.command.help.HelpEntry;
import net.forthecrown.command.help.Usage;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.text.TextWriters;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.page.Footer;
import net.forthecrown.text.page.Header;
import net.forthecrown.text.page.PageEntry;
import net.forthecrown.text.page.PagedIterator;
import net.forthecrown.text.page.PageFormat;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.slf4j.Logger;

public class HelpListImpl implements FtcHelpList {

  public static final Logger LOGGER = Loggers.getLogger();

  private static final Comparator<ObjectIntPair<HelpEntry>> COMPARATOR;

  private static final Comparator<HelpEntry> ALPHABETIC_COMPARATOR
      = Comparator.comparing(HelpEntry::getMainLabel);

  /** Map of all keywords, mapped to their bound values */
  private final Map<String, Collection<HelpEntry>> keywordLookup
      = new Object2ObjectOpenHashMap<>();

  /** Map of all existing commands, mapped to their name */
  private final Map<String, FtcCommand> existingCommands = new HashMap<>();

  private final List<HelpEntry> entries = new ObjectArrayList<>();
  private final List<LoadedHelpEntry> loadedEntries = new ObjectArrayList<>();

  // Context used to pass data onto the page formatter
  private final ContextSet contextSet = ContextSet.create();

  private final ContextOption<CommandSource> sourceOption
      = contextSet.newOption();

  private final ContextOption<String> inputOption
      = contextSet.newOption();

  private final ContextOption<Integer> actualPageSize
      = contextSet.newOption(5);

  // Page format used to format entries for list-based display
  private final PageFormat<HelpEntry> pageFormat = PageFormat.create();

  private final PageFormat<Component> singleEntryPaginator
      = PageFormat.create();

  private final Path file;

  static {
    Comparator<ObjectIntPair<HelpEntry>> cmp = Comparator.comparingInt(ObjectIntPair::rightInt);
    COMPARATOR = cmp.thenComparing(pair -> pair.left().getMainLabel());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public HelpListImpl() {
    this.file = PathUtil.pluginPath("help_topics.json");

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
  @Override
  public CompletableFuture<Suggestions> suggest(CommandSource source, SuggestionsBuilder builder) {
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
        .filter(s -> Completions.matches(unquoted, s))
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
  @Override
  public Component query(
      CommandSource source,
      String tag,
      int page,
      int pageSize
  ) throws CommandSyntaxException {
    List<HelpEntry> entries = new ObjectArrayList<>();

    if (Strings.isNullOrEmpty(tag) || tag.equalsIgnoreCase("all")) {
      entries.addAll(getAll());

      // Remove the ones the source doesn't have permission to see
      entries.removeIf(entry -> !entry.test(source));
      entries.sort(ALPHABETIC_COMPARATOR);
    } else {
      List<ObjectIntPair<HelpEntry>> lookupResult = lookup(normalize(tag), source);
      lookupResult.sort(COMPARATOR);
      lookupResult.stream().map(Pair::left).forEach(entries::add);
    }

    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.YELLOW));
    writer.placeholders(Placeholders.newRenderer().useDefaults());

    Context context = contextSet.createContext()
        .set(sourceOption, source)
        .set(inputOption, tag)
        .set(actualPageSize, pageSize);

    // Single entry, write that 1 entry
    if (entries.size() == 1) {
      pageSize += 5;
      var entry = entries.iterator().next();
      var loreWriter = TextWriters.buffered();

      loreWriter.setFieldStyle(Style.style(NamedTextColor.YELLOW));
      entry.writeFull(loreWriter, source);

      var text = loreWriter.getBuffer();

      // Ensure list isn't empty and page number is valid
      Commands.ensurePageValid(page, pageSize, text.size());

      var it = PagedIterator.of(text, page, pageSize);
      singleEntryPaginator.write(it, writer, context);

      return writer.asComponent();
    }

    // Ensure list isn't empty and page number is valid
    Commands.ensurePageValid(page, pageSize, entries.size());

    // Format all results onto a page
    var iterator = PagedIterator.of(entries, page, pageSize);

    pageFormat.write(iterator, writer, context);
    return writer.asComponent();
  }

  private List<ObjectIntPair<HelpEntry>> lookup(String tag, CommandSource source) {
    // Try just calling the keyword lookup
    Collection<HelpEntry> entries = getEntries(tag);
    entries.removeIf(entry -> !entry.test(source));

    if (!entries.isEmpty()) {
      List<ObjectIntPair<HelpEntry>> result = new ObjectArrayList<>();
      for (HelpEntry entry : entries) {
        result.add(ObjectIntPair.of(entry, 0));
      }
      return result;
    }

    // Keyword lookup failed, loop through all keywords to find the ones
    // that match the most
    List<ObjectIntPair<HelpEntry>> result = new ObjectArrayList<>();
    final int maxDistance = 3;

    for (var v: getAll()) {
      if (!v.test(source)) {
        continue;
      }

      var keywords = v.getKeywords();

      for (var keyword: keywords) {
        var s = normalize(keyword);
        int dis = levenshteinDistance(tag, s);

        // -1 means above max threshold
        if (dis == -1 || dis > maxDistance) {
          continue;
        }

        // I STG If this condition ever returns true, I will throw
        // someone or something out of a window
        if (dis == 0) {
          result = new ObjectArrayList<>();
          result.add(ObjectIntPair.of(v, dis));
          return result;
        }

        result.add(ObjectIntPair.of(v, dis));
        break;
      }
    }

    return result;
  }

  // Copy-pasted from org.bukkit.command.defaults.HelpCommand
  private static int levenshteinDistance(String s1, String s2) {
    if (s1 == null && s2 == null) {
      return 0;
    }
    if (s1 != null && s2 == null) {
      return s1.length();
    }
    if (s1 == null && s2 != null) {
      return s2.length();
    }

    int s1Len = s1.length();
    int s2Len = s2.length();
    int[][] H = new int[s1Len + 2][s2Len + 2];

    int INF = s1Len + s2Len;
    H[0][0] = INF;
    for (int i = 0; i <= s1Len; i++) {
      H[i + 1][1] = i;
      H[i + 1][0] = INF;
    }
    for (int j = 0; j <= s2Len; j++) {
      H[1][j + 1] = j;
      H[0][j + 1] = INF;
    }

    Char2IntMap sd = new Char2IntOpenHashMap();
    sd.defaultReturnValue(0);

    for (int i = 1; i <= s1Len; i++) {
      int DB = 0;
      for (int j = 1; j <= s2Len; j++) {
        int i1 = sd.get(s2.charAt(j - 1));
        int j1 = DB;

        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          H[i + 1][j + 1] = H[i][j];
          DB = j;
        } else {
          H[i + 1][j + 1] = Math.min(H[i][j], Math.min(H[i + 1][j], H[i][j + 1])) + 1;
        }

        H[i + 1][j + 1] = Math.min(H[i + 1][j + 1], H[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1));
      }
      sd.put(s1.charAt(i - 1), i);
    }

    return H[s1Len + 1][s2Len + 1];
  }

  private Collection<HelpEntry> getAll() {
    return entries;
  }

  public Collection<FtcCommand> getExistingCommands() {
    return Collections.unmodifiableCollection(existingCommands.values());
  }

  public Collection<HelpEntry> getAllEntries() {
    return Collections.unmodifiableCollection(entries);
  }

  public Collection<HelpEntry> getEntries(String keyword) {
    var list = keywordLookup.getOrDefault(keyword, ObjectLists.emptyList());

    if (list.isEmpty()) {
      return list;
    }

    return new ObjectArrayList<>(list);
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
    keywordLookup.clear();
    entries.removeIf(entry -> entry instanceof CommandHelpEntry);

    existingCommands.forEach((s, command) -> {
      if (command instanceof LoadedEntryCommand) {
        return;
      }

      CommandHelpEntry entry = new CommandHelpEntry(command);

      UsageFactory factory = arguments -> {
        Usage usage = new Usage(arguments);
        entry.getUsages().add(usage);
        return usage;
      };

      if (command.isSimpleUsages()) {
        factory.usage("").addInfo(command.getDescription());
      }

      command.populateUsages(factory);
      entries.add(entry);
    });

    entries.forEach(this::placeInLookup);
  }

  @Override
  public void addEntry(HelpEntry entry) {
    entries.add(entry);
    placeInLookup(entry);
  }

  private void placeInLookup(HelpEntry entry) {
    entry.getKeywords().forEach(s -> {
      Collection<HelpEntry> entries = keywordLookup.computeIfAbsent(
          normalize(s), s1 -> new ObjectArrayList<>()
      );

      entries.add(entry);
    });
  }

  private static String normalize(String s) {
    if (s.startsWith("/")) {
      s = s.substring(1);
    }

    return s.toLowerCase()
        .trim()
        .replaceAll(" ", "_");
  }

  private void clearDynamicallyLoaded() {
    keywordLookup.forEach((label, entries) -> {
      entries.removeIf(entry -> entry instanceof LoadedHelpEntry);
    });
    entries.removeIf(entry -> entry instanceof LoadedHelpEntry);
    existingCommands.values().removeIf(cmd -> cmd instanceof LoadedEntryCommand);

    CommandDispatcher<CommandSource> dispatcher = Grenadier.dispatcher();

    for (LoadedHelpEntry entry : loadedEntries) {
      if (entry.getCommand() == null) {
        continue;
      }

      var cmd = entry.getCommand();
      Commands.removeChild(dispatcher.getRoot(), cmd.getName());
    }

    loadedEntries.clear();
  }

  public void load() {
    clearDynamicallyLoaded();

    PluginJar.saveResources("help_topics.json");
    SerializationHelper.readJsonFile(file, this::loadFrom);
  }

  private void loadFrom(JsonWrapper json) {
    for (Entry<String, JsonElement> entry : json.entrySet()) {
      if (!entry.getValue().isJsonObject()) {
        LOGGER.error("Help entry {} is not an object", entry.getKey());
        continue;
      }

      JsonWrapper entryJson = JsonWrapper.wrap(entry.getValue().getAsJsonObject());

      Set<String> labels = new ObjectOpenHashSet<>();
      labels.add(entry.getKey());
      labels.addAll(entryJson.getList("aliases", JsonElement::getAsString));

      ViewerAwareMessage shortText = entryJson.get("shortText", JsonUtils::readMessage);
      ViewerAwareMessage fullText = entryJson.get("fullText", JsonUtils::readMessage);

      boolean makeCommand = entryJson.getBool("make_command");

      LoadedHelpEntry helpEntry = new LoadedHelpEntry(labels, entry.getKey(), shortText, fullText);

      if (makeCommand) {
        helpEntry.setCommand(new LoadedEntryCommand(entry.getKey(), helpEntry));
        helpEntry.getCommand().register();
      }

      addEntry(helpEntry);
      loadedEntries.add(helpEntry);
    }
  }
}