package net.forthecrown.commands.manager;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
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
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

public abstract class FtcCommand
    extends AbstractCommand
    implements HoverEventSource<Component>
{
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

    populateUsages(arguments -> {
      var us = new Usage(arguments);
      usages.add(us);
      return us;
    });

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

  @Override
  public @NotNull HoverEvent<Component> asHoverEvent(
      @NotNull UnaryOperator<Component> op
  ) {
    TextWriter writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));

    writer.write("/" + getHelpListName());
    writeMetadata(writer);

    return writer.asComponent().asHoverEvent(op);
  }

  /** Writes this command's aliases and description to the given writer */
  public void writeMetadata(TextWriter writer) {
    if (aliases != null) {
      writer.field("Aliases", Joiner.on(", ").join(aliases));
    }

    if (!Strings.isNullOrEmpty(getDescription())
        && !getDescription().contains(DEFAULT_DESCRIPTION)
    ) {
      writer.field("Description", getDescription());
    }

    if (!usages.isEmpty()) {
      writer.field("Usages", usages.size());
    }
  }

  /** A function which generates Usage instances */
  @FunctionalInterface
  public interface UsageFactory {
    Usage create(String arguments);

    default UsageFactory withPrefix(String prefix) {
      return arguments -> create(
          prefix + (Strings.isNullOrEmpty(arguments) ? "" : " " + arguments)
      );
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
    private Predicate<CommandSource> condition
        = ArgumentBuilder.defaultRequirement();

    public Usage addInfo(String info) {
      this.info = ArrayUtils.add(this.info, info);
      return this;
    }

    public String argumentsWithPrefix(String prefix) {
      return prefix + (Strings.isNullOrEmpty(arguments) ? "" : " " + arguments);
    }
  }
}