package net.forthecrown.command.help;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public record CommandDisplayInfo(
    String label,
    String permission,
    Component description,
    Predicate<CommandSource> predicate,
    List<Usage> usages,
    List<String> aliases,
    String category
) {

  public String getDescription() {
    return description == null
        ? FtcCommand.DEFAULT_DESCRIPTION
        : Text.plain(description);
  }

  public static CommandDisplayInfo create(
      Either<GrenadierCommand, GrenadierCommandNode> either,
      List<Usage> usages,
      @Nullable String label,
      String category
  ) {
    Objects.requireNonNull(category, "Category null");
    Objects.requireNonNull(usages, "Usages null");

    return either.map(command -> {
      String cmdLabel = Strings.isNullOrEmpty(label)
          ? command.getLiteral()
          : label;

      return new CommandDisplayInfo(
          cmdLabel,
          command.getPermission(),
          command.description(),
          command.getRequirement(),
          usages,
          command.getAliases(),
          category
      );
    }, node -> {
      String cmdLabel = Strings.isNullOrEmpty(label)
          ? node.getLiteral()
          : label;

      return new CommandDisplayInfo(
          cmdLabel,
          node.getPermission(),
          node.description(),
          node.getRequirement(),
          usages,
          node.getAliases(),
          category
      );
    });
  }

  public boolean test(CommandSource source) {
    if (!Strings.isNullOrEmpty(permission)
        && !source.hasPermission(permission)
    ) {
      return false;
    }

    return predicate.test(source);
  }

  public List<Usage> usagesFor(CommandSource source) {
    return usages.stream()
        .filter(usage -> usage.getCondition().test(source))
        .toList();
  }

  private String usagePrefix() {
    return "/" + label;
  }

  public HoverEvent<Component> asHover(CommandSource source) {
    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));

    writer.formatted("/{0}", label());
    writeMetadata(writer, source);

    var usages = usagesFor(source);

    if (!usages.isEmpty()) {
      writer.field("Usages", "");
      usages.forEach(usage -> writer.line(usage.argumentsWithPrefix(usagePrefix())));
    }

    return writer.asComponent().asHoverEvent();
  }

  public void writeMetadata(TextWriter writer, CommandSource source) {
    if (!test(source)) {
      return;
    }

    var aliases = aliases();
    if (aliases != null && !aliases.isEmpty()) {
      writer.field("Aliases", Joiner.on(", ").join(aliases));
    }

    if (description != null) {
      writer.field("Description", description());
    }

    if (source.hasPermission(Permissions.ADMIN)) {
      writer.field("Permission", permission == null ? "unset" : permission);

      if (!Strings.isNullOrEmpty(category)) {
        writer.field("Category", category);
      }
    }
  }

  public void writeUsages(TextWriter writer,
                          CommandSource source,
                          boolean includeTitle
  ) {
    if (!test(source)) {
      return;
    }

    var usages = usagesFor(source);

    if (usages.isEmpty()) {
      return;
    }

    if (includeTitle) {
      writer.newLine();
      writer.newLine();
      writer.field("Usages", "");
    }

    for (var n : usages) {
      writer.line(n.argumentsWithPrefix(usagePrefix()));

      if (!ArrayUtils.isEmpty(n.getInfo())) {
        writer.write(":");
      }

      Arrays.stream(n.getInfo())
          .forEach(s -> writer.line("  " + s, NamedTextColor.GRAY));
    }
  }
}