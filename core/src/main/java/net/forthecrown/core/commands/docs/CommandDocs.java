package net.forthecrown.core.commands.docs;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.command.help.AbstractHelpEntry;
import net.forthecrown.command.help.CommandDisplayInfo;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.command.help.Usage;
import net.forthecrown.text.Text;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.io.PathUtil;

@Getter @Setter
@RequiredArgsConstructor
public class CommandDocs {

  private final Map<String, List<CommandDocument>> documents
      = new Object2ObjectOpenHashMap<>();

  private boolean removeSquareBrackets;
  private boolean genWikiHeader;
  private boolean genContentTable;
  private boolean genIdTags;

  private Collection<String> included = List.of();
  private Collection<String> excluded = List.of();

  public void fill() {
    boolean noCustomFilter = included.isEmpty() && excluded.isEmpty();

    FtcHelpList.helpList()
        .getAllEntries()
        .stream()
        .filter(entry -> entry instanceof AbstractHelpEntry)
        .map(entry -> (AbstractHelpEntry) entry)

        .filter(entry -> {
          if (noCustomFilter) {
            return true;
          }

          Collection<String> keywords = new HashSet<>(entry.getKeywords());
          keywords.add("#" + entry.getCategory());

          boolean includeFound = included.isEmpty();

          for (String keyword : keywords) {
            if (excluded.contains(keyword)) {
              return false;
            }
            if (included.contains(keyword)) {
              includeFound = true;
            }
          }

          return includeFound;
        })

        .forEach(this::createDocumentation);

    documents.forEach((key, value) -> {
      value.sort(Comparator.comparing(document -> document.name));
    });


  }

  private void createDocumentation(AbstractHelpEntry command) {
    CommandDisplayInfo display = command.createDisplay();

    var category = display.category();
    if (Strings.isNullOrEmpty(category)) {
      category = "uncategorized";
    }

    if (category.contains("help")
        || category.contains("test")
        || category.contains("click")
    ) {
      return;
    }

    String name = display.label();
    String perm = display.permission();
    String desc = display.getDescription();
    List<String> aliases = display.aliases();

    CommandDocument document = new CommandDocument(name, perm, aliases, desc);
    document.usages.addAll(command.getUsages());

    document.clickId = category.toLowerCase()
        .replace("_", ".")
        .replace(".", "-");

    List<CommandDocument> list = documents.computeIfAbsent(category, c -> new ObjectArrayList<>());
    list.add(document);
  }

  public void writeSingleton(Path output) throws IOException {
    PathUtil.ensureParentExists(output);

    try (var writer = Files.newBufferedWriter(output)) {
      if (genWikiHeader) {
        writeHugoHeader(writer, "commands", "All");
        writer.newLine();
      }

      // Table of contents
      if (genContentTable) {
        writer.write("# Table of contents");
        writer.newLine();

        int index = 1;

        for (var e: documents.entrySet()) {
          var name = e.getKey();

          writer.write(index++ + ". ");

          if (genIdTags) {
            writer.write("[");
            writer.write(name);
            writer.write("](#");
            writer.write(name);
            writer.write(")");
          } else {
            writer.write(name);
          }

          writer.newLine();

          //int subIndex = 1;

          // Write sub paragraphs
          for (var v: e.getValue()) {
            writer.write("    ");
            // writer.write(subIndex++ + ". ");
            writer.write("- ");

            // Sub-paragraphs don't work, the hyperlinks in the titles don't
            // do anything
            // writer.write("[");

            writer.write("/");
            writer.write(v.name);

            // writer.write("]");
            // writer.write("(#");
            // writer.write(v.clickId);
            // writer.write(")");
            writer.newLine();
          }
        }
      }

      int totalCommands = 0;

      // Write contents
      for (var e: documents.entrySet()) {
        var name = e.getKey();
        var id = e.getKey();

        writer.write("## ");
        writer.write(name);

        if (genIdTags) {
          writer.write(" <a id=\"");
          writer.write(id);
          writer.write("\"></a>");
        }

        for (var d: e.getValue()) {
          d.write(writer, 3);
          ++totalCommands;
        }
      }

      // Write footer
      writeMetadata(writer, totalCommands);
    }
  }

  public void writeSeparated(Path outputDirectory) throws IOException {
    for (var e: documents.entrySet()) {
      String pName = e.getKey();
      Path path = outputDirectory.resolve(pName.toLowerCase().replace("/", "-") + ".md");

      PathUtil.ensureParentExists(path);

      BufferedWriter writer = Files.newBufferedWriter(path);
      writeCategory(e.getValue(), pName, writer);
      writer.close();
    }
  }

  private void writeHugoHeader(BufferedWriter writer, String title, String desc)
      throws IOException
  {
    writer.write("---");

    writer.newLine();
    writer.write("title: ");
    writer.write('"');
    writer.write(title);
    writer.write('"');

    writer.newLine();
    writer.write("linkTitle: ");
    writer.write('"');
    writer.write(title);
    writer.write('"');

    writer.newLine();
    writer.write("type: docs");

    writer.newLine();
    writer.write("weight: 1");

    writer.newLine();
    writer.write("description: >");

    writer.newLine();
    writer.write("  ");
    writer.write(desc);
    writer.write(" commands");

    writer.newLine();
    writer.write("---");
  }

  private void writeCategory(
      List<CommandDocument> documents,
      String packageName,
      BufferedWriter writer
  ) throws IOException {
    if (genWikiHeader) {
      writeHugoHeader(writer, packageName, packageName);
      writer.newLine();
      writer.newLine();
    }

    if (genContentTable) {
      writer.write("# Table of Contents");
      writer.newLine();

      for (var d: documents) {
        writer.write("- ");

        if (!genIdTags) {
          writer.write(d.name);
          writer.newLine();
          continue;
        }

        writer.write("[/");
        writer.write(d.name);
        writer.write("](#");
        writer.write(d.clickId);
        writer.write(")");
        writer.newLine();
      }

      writer.newLine();
      writer.write("# Commands");
    }

    for (var d: documents) {
      d.write(writer, 2);
    }

    writeMetadata(writer, documents.size());
  }

  private void writeMetadata(BufferedWriter writer, int written) throws IOException {
    writer.newLine();
    writer.write("# Metadata");
    writer.newLine();
    writer.write("This is an auto-generated command documentation file");
    writer.write(" generated by the FTC plugin.");

    writer.write("  ");
    writer.newLine();

    writer.write("Date: `");
    writer.write(new Date().toString());
    writer.write("`");

    writer.write("  ");
    writer.newLine();

    writer.write("Plugin version: `");
    writer.write(PluginUtil.getPlugin().getPluginMeta().getVersion());
    writer.write("`");

    writer.write("  ");
    writer.newLine();

    writer.write("Total commands: ");
    writer.write(Text.NUMBER_FORMAT.format(written));
  }

  @RequiredArgsConstructor
  class CommandDocument {
    private final String name;
    private final String permission;
    private final List<String> aliases;
    private final String description;
    private final List<Usage> usages = new ObjectArrayList<>();

    private String clickId;

    public void write(BufferedWriter writer, int headerLevel) throws IOException {
      writer.newLine();
      writer.write("#".repeat(headerLevel));
      writer.write(" /");
      writer.write(name);

      if (genIdTags) {
        writer.write(" <a id=\"");
        writer.write(clickId);
        writer.write("\"></a>");
      }

      writer.newLine();

      if (!Strings.isNullOrEmpty(description)) {
        writer.write(description);

        if (description.equalsIgnoreCase("An FTC command")) {
          writer.write(" (default description)");
        }

        writer.write("  ");
        writer.newLine();
        writer.write("  ");
        writer.newLine();
      }

      if (!Strings.isNullOrEmpty(permission)) {
        writer.write("**Permission**: `" + permission + "`  ");
        writer.newLine();
      }

      if (!aliases.isEmpty()) {
        StringJoiner joiner = new StringJoiner("`, `", "`", "`");
        for (var s: aliases) {
          joiner.add(s);
        }

        writer.write("**Aliases**: " + joiner);
        writer.write("  ");
        writer.newLine();
      }

      if (usages.isEmpty()) {
        return;
      }

      writeUsages(writer);
    }

    void writeUsages(BufferedWriter writer) throws IOException {
      writer.write("**Uses**:");
      writer.newLine();

      for (Usage usage : usages) {
        writer.write("- ");

        writer.write("<pre class=\"command-usage-arguments\">");
        String args = usage.argumentsWithPrefix(name);
        writer.write("/" + filterUsageText(args));
        writer.write("</pre>  ");
        writer.newLine();

        String[] info = usage.getInfo();
        for (String s : info) {
          writer.write("  ");

          if (removeSquareBrackets) {
            s = s.replaceAll("[<>]+", "");
          }

          writer.write(filterUsageText(s));

          writer.write("  ");
          writer.newLine();
        }
      }
    }

    String filterUsageText(String str) {
      return str
          .replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;");
    }

    void writeUsagesAsBlock(BufferedWriter writer)
        throws IOException
    {
      writer.write("### Usages:");
      writer.newLine();
      writer.write("```yaml");
      writer.newLine();

      var uIt = usages.iterator();
      while (uIt.hasNext()) {
        var s = uIt.next();

        writer.write("/" + s.argumentsWithPrefix(name) + "");
        writer.newLine();

        if (s.getInfo().length > 0) {
          var it = ArrayIterator.unmodifiable(s.getInfo());

          while (it.hasNext()) {
            writer.write("# " + it.next());

            if (it.hasNext()) {
              writer.newLine();
            }
          }
        }

        if (uIt.hasNext()) {
          writer.newLine();
          writer.newLine();
        }
      }

      writer.newLine();
      writer.write("```");
      writer.newLine();
    }
  }
}