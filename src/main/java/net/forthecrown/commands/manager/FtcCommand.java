package net.forthecrown.commands.manager;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.commands.help.FtcHelpMap;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

public abstract class FtcCommand extends AbstractCommand {
  public static final String DEFAULT_DESCRIPTION = "An FTC command";

  @Getter
  private final LinkedList<Usage> usages = new LinkedList<>();

  protected FtcCommand(@NotNull String name) {
    super(name, FTC.getPlugin());

    // unknown command for permission message cuz you
    // don't need to know what kinds of commands we have
    permissionMessage(Messages.UNKNOWN_COMMAND);

    setPermission(Permissions.registerCmd(name));
    setDescription(DEFAULT_DESCRIPTION);

    FtcHelpMap.getInstance()
        .addCommand(this);
  }

  public String getHelpListName() {
    return getName();
  }

  protected static User getUserSender(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    return Users.get(c.getSource().asPlayer());
  }

  public Collection<String> createKeywords() {
    return Set.of();
  }

  /**
   * Populates the {@link #usages} list
   * @param factory The factory that generates {@link Usage} instances
   */
  public void populateUsages(UsageFactory factory) {
  }

  public void simpleUsages() {
    usages.add(new Usage("").addInfo(getDescription()));
  }

  public @NotNull HoverEvent<Component> asHoverEvent(CommandSource source) {
    TextWriter writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));

    writer.write("/" + getHelpListName());
    writeMetadata(writer, source);

    LinkedList<Usage> usages = (LinkedList<Usage>) getUsages().clone();
    usages.removeIf(usage -> !usage.getCondition().test(source));

    if (!usages.isEmpty()) {
      writer.field("Usages", "");
      usages.forEach(usage -> {
        writer.line(usage.argumentsWithPrefix("/" + getHelpListName()));
      });
    }

    return writer.asComponent()
        .asHoverEvent();
  }

  /** Writes this command's aliases and description to the given writer */
  public void writeMetadata(TextWriter writer, CommandSource source) {
    if (!test(source)) {
      return;
    }

    if (aliases != null) {
      writer.field("Aliases", Joiner.on(", ").join(aliases));
    }

    if (!Strings.isNullOrEmpty(getDescription())
        && !getDescription().contains(DEFAULT_DESCRIPTION)
    ) {
      writer.field("Description", getDescription());
    }

    if (source.hasPermission(Permissions.ADMIN)) {
      var perm = getPermission();
      writer.field("Permission", perm == null ? "unset" : perm.getName());
    }
  }

  public void writeUsages(TextWriter writer,
                          CommandSource source,
                          boolean includeTitle
  ) {
    if (!test(source)) {
      return;
    }

    LinkedList<Usage> usages = (LinkedList<Usage>) getUsages().clone();
    usages.removeIf(usage -> !usage.getCondition().test(source));

    if (usages.isEmpty()) {
      return;
    }

    if (includeTitle) {
      writer.newLine();
      writer.newLine();
      writer.field("Usages", "");
    }

    for (var n : usages) {
      writer.line(n.argumentsWithPrefix("/" + getHelpListName()));

      if (!ArrayUtils.isEmpty(n.getInfo())) {
        writer.write(":");
      }

      Arrays.stream(n.getInfo())
          .forEach(s -> writer.line("  " + s, NamedTextColor.GRAY));
    }
  }

  /** A function which generates Usage instances */
  @FunctionalInterface
  public interface UsageFactory {
    Usage usage(String arguments);

    default Usage usage(String argument, String... usages) {
      var usage = usage(argument);

      for (var s: usages) {
        usage.addInfo(s);
      }

      return usage;
    }

    default UsageFactory withPrefix(String prefix) {
      return arguments -> usage(
          prefix + (Strings.isNullOrEmpty(arguments) ? "" : " " + arguments)
      );
    }

    default UsageFactory withCondition(Predicate<CommandSource> predicate) {
      return arguments -> usage(arguments).setCondition(predicate);
    }

    default UsageFactory withPermission(Permission permission) {
      return arguments -> usage(arguments).setPermission(permission);
    }
  }

  /**
   * A simple piece of data that represents a single usage text for a command.
   */
  @Getter
  @RequiredArgsConstructor
  public static class Usage {
    /** The usage argument, example: <code>/pay (user) (amount: number)</code> */
    private final String arguments;

    /**
     * Info to represent the above arguments, each array value representing
     * a single line of info
     */
    private String[] info = new String[0];

    /**
     * The condition that must be passed for this usage to be displayed
     * to command sources.
     * <p>
     * By default, just always returns true
     */
    @Setter
    @Accessors(chain = true)
    private Predicate<CommandSource> condition = source -> true;

    public Usage setPermission(Permission permission) {
      return setCondition(source -> source.hasPermission(permission));
    }

    public Usage addInfo(String info, Object... args) {
      this.info = ArrayUtils.add(this.info, info.formatted(args));
      return this;
    }

    public String argumentsWithPrefix(String prefix) {
      return prefix + (Strings.isNullOrEmpty(arguments) ? "" : " " + arguments);
    }
  }
}