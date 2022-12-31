package net.forthecrown.commands.docs;

import com.google.common.base.Strings;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.arguments.chat.ChatArgument;
import net.forthecrown.commands.help.FtcHelpMap;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcCommand.Usage;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.text.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.compare.ComparableUtils;

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

  private String getTypeString(ArgumentType<?> type) {
    if (type instanceof StringArgumentType arg) {
      return switch (arg.getType()) {
        case SINGLE_WORD -> "word";
        case GREEDY_PHRASE -> "greedy string";
        case QUOTABLE_PHRASE -> "string";
      };
    }

    if (type instanceof IntegerArgumentType intArg) {
      return numberArgument(
          intArg.getMinimum(), intArg.getMaximum(),
          Integer.MIN_VALUE, Integer.MAX_VALUE
      );
    }

    if (type instanceof LongArgumentType longArg) {
      return numberArgument(
          longArg.getMinimum(), longArg.getMaximum(),
          Long.MIN_VALUE, Long.MAX_VALUE
      );
    }

    if (type instanceof FloatArgumentType floatArg) {
      return numberArgument(
          floatArg.getMinimum(), floatArg.getMaximum(),
          Float.MIN_VALUE, Float.MAX_VALUE
      );
    }

    if (type instanceof DoubleArgumentType doubleArg) {
      return numberArgument(
          doubleArg.getMinimum(), doubleArg.getMaximum(),
          Double.MIN_VALUE, Double.MAX_VALUE
      );
    }

    if (type instanceof UserArgument userArg) {
      if (userArg.allowMultiple) {
        return userArg.allowOffline ? "users" : "online users";
      } else {
        return userArg.allowOffline ? "user" : "online user";
      }
    }

    if (type instanceof EntityArgument e) {
      if (e.allowsEntities()) {
        return e.allowsMultiple() ? "entities" : "entity";
      } else {
        return e.allowsMultiple() ? "online users" : "online user";
      }
    }

    if (type instanceof RegistryArguments<?> reg) {
      return reg.getUnknown().toLowerCase();
    }

    if (type instanceof ChatArgument) {
      return "message";
    }

    if (type instanceof ArrayArgument<?> arr) {
      return "array(" + getTypeString(arr.getType()) + ")";
    }

    if (type instanceof EnumArgument<?> enumArg) {
      return enumArg.getEnumType()
          .getSimpleName();
    }

    if (type instanceof ArgsArgument args) {
      StringJoiner joiner = new StringJoiner(", ", "(", ")")
          .setEmptyValue("");

      Set<Argument<?>> alreadyPicked = new ObjectOpenHashSet<>();

      for (var k: args.getKeys()) {
        Argument argument = args.getArg(k);

        if (!alreadyPicked.add(argument)) {
          continue;
        }

        joiner.add(
            argument.getName() + ": " + getTypeString(argument.getParser())
        );
      }

      return "args" + joiner;
    }

    return type.getClass()
        .getSimpleName()
        .toLowerCase()
        .replaceAll("type", "")
        .replaceAll("ftc", "")
        .replaceAll("impl", "")
        .replaceAll("argument", "");
  }

  private <T extends Comparable<T>> String numberArgument(T min, T max,
                                                          T possibleMin, T possibleMax
  ) {
    StringJoiner joiner = new StringJoiner("...", "(", ")")
        .setEmptyValue("");

    boolean minSet = ComparableUtils.gt(possibleMin).test(min);
    boolean maxSet = ComparableUtils.lt(possibleMax).test(max);

    if (minSet) {
      joiner.add(Text.NUMBER_FORMAT.format(min));
    } else if (maxSet) {
      joiner.add("");
    }

    if (maxSet) {
      joiner.add(Text.NUMBER_FORMAT.format(max));
    } else if (minSet) {
      joiner.add("");
    }

    return "number" + joiner;
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