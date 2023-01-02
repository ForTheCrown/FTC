package net.forthecrown.commands.docs;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.help.FtcHelpMap;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcCommand.Usage;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.lang3.ArrayUtils;

@RequiredArgsConstructor
public class CommandDocs {
  private final boolean addDocumentStubs;

  private final Map<Package, List<CommandDocument>> documents
      = new Object2ObjectOpenHashMap<>();

  public void write(Path directory) {
    for (var e: documents.entrySet()) {
      String fileName = e.getKey().getName()
          .replaceAll("net.forthecrown.commands.", "");

      if (fileName.equals("net.forthecrown.commands")) {
        fileName = "uncategorized";
      }

      Path path = directory.resolve(fileName + ".md");

      SerializationHelper.writeFile(path, file -> {
        var writer = Files.newBufferedWriter(file);

        for (var d: e.getValue()) {
          d.write(writer);
        }

        writer.close();
      });
    }
  }

  public void fill() {
    FtcHelpMap.getInstance()
        .getExistingCommands()
        .forEach(this::createDocumentation);
  }

  public void createDocumentation(FtcCommand command) {
    String name = "/" + command.getName();
    String perm = command.getPerm();
    String desc = command.getDescription();
    String[] aliases = command.getAliases();

    CommandDocument document = new CommandDocument(name, perm, aliases, desc);
    document.usages.addAll(command.getUsages());

    var list = documents.computeIfAbsent(
        command.getClass().getPackage(),
        aPackage -> new ObjectArrayList<>()
    );
    list.add(document);
  }

  @RequiredArgsConstructor
  class CommandDocument {
    private final String name;
    private final String permission;
    private final String[] aliases;
    private final String description;
    private final List<Usage> usages = new ObjectArrayList<>();

    public void write(BufferedWriter writer) throws IOException {
      writer.newLine();
      writer.write("## `" + name + "`");
      writer.newLine();
      writer.write("### Metadata");
      writer.newLine();

      if (!Strings.isNullOrEmpty(permission)) {
        writer.write("Permission: `" + permission + "`  ");
        writer.newLine();
      }

      if (!Strings.isNullOrEmpty(description)) {
        writer.write("Description: `" + description + "`  ");
        writer.newLine();
      }

      if (!ArrayUtils.isEmpty(aliases)) {
        StringJoiner joiner = new StringJoiner("`, `", "`", "`");
        for (var s: aliases) {
          joiner.add(s);
        }

        writer.write("Aliases: " + joiner);
        writer.write("  ");
        writer.newLine();
      }

      if (usages.isEmpty()) {
        return;
      }

      writer.write("### Arguments");
      writer.newLine();

      for (var s: usages) {
        writer.write("**`" + s.argumentsWithPrefix(name) + "`**");
        writer.write(":  ");
        writer.newLine();

        if (s.getInfo().length > 0) {
          var it = ArrayIterator.unmodifiable(s.getInfo());

          while (it.hasNext()) {
            writer.write("> " + it.next());

            if (it.hasNext()) {
              writer.write("  ");
              writer.newLine();
            }
          }
        } else if (addDocumentStubs) {
          writer.write("> No documentation given :(  ");
        }

        writer.newLine();
        writer.newLine();
      }
    }
  }
}