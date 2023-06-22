package net.forthecrown.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.StackWalker.Option;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.ExpandedEntityArgument;
import net.forthecrown.command.help.FtcSyntaxConsumer;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext.DefaultExecutionRule;
import net.forthecrown.grenadier.annotations.CommandDataLoader;
import net.forthecrown.grenadier.annotations.TypeRegistry;
import net.forthecrown.text.page.PageEntryIterator;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class Commands {
  private Commands() {}

  public static final String DEFAULT_PERMISSION_FORMAT = "ftc.commands.{command}";

  public static AnnotatedCommandContext createAnnotationContext() {
    AnnotatedCommandContext ctx = AnnotatedCommandContext.create();

    ctx.setDefaultRule(DefaultExecutionRule.IF_NO_CHILDREN);
    ctx.setDefaultPermissionFormat(DEFAULT_PERMISSION_FORMAT);
    ctx.setDefaultExecutes("execute");
    ctx.setTypeRegistry(createFtcTypeRegistry());
    ctx.setSyntaxConsumer(new FtcSyntaxConsumer());

    Class<?> caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();

    CommandDataLoader loader = CommandDataLoader.resources(caller.getClassLoader());
    ctx.addLoader(loader);

    return ctx;
  }

  public static TypeRegistry createFtcTypeRegistry() {
    var registry = TypeRegistry.newRegistry();
    registry.register("user",         () -> Arguments.USER);
    registry.register("users",        () -> Arguments.USERS);
    registry.register("online_user",  () -> Arguments.ONLINE_USER);
    registry.register("online_users", () -> Arguments.ONLINE_USERS);
    registry.register("chat",         () -> Arguments.CHAT);
    registry.register("message",      () -> Arguments.MESSAGE);
    registry.register("ftc_key",      () -> Arguments.FTC_KEY);
    registry.register("rhines",       () -> Arguments.RHINES);

    registry.register("f_player",     () -> new ExpandedEntityArgument(false, true));
    registry.register("f_players",    () -> new ExpandedEntityArgument(true, true));
    registry.register("f_entity",     () -> new ExpandedEntityArgument(false, false));
    registry.register("f_entities",   () -> new ExpandedEntityArgument(true, false));

    return registry;
  }

  public static void ensureIndexValid(int index, int size) throws CommandSyntaxException {
    if (index > size) {
      throw Exceptions.invalidIndex(index, size);
    }
  }

  public static void ensurePageValid(int page, int pageSize, int size)
      throws CommandSyntaxException
  {
    if (size == 0) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    var max = PageEntryIterator.getMaxPage(pageSize, size);

    if (page >= max) {
      throw Exceptions.invalidPage(page + 1, max);
    }
  }

  public static ItemStack getHeldItem(Player player) throws CommandSyntaxException {
    var item = player.getInventory().getItemInMainHand();

    if (ItemStacks.isEmpty(item)) {
      throw Exceptions.MUST_HOLD_ITEM;
    }

    return item;
  }

  public static String optionallyQuote(String quote, String s) {
    for (var c: s.toCharArray()) {
      if (!StringReader.isAllowedInUnquotedString(c)) {
        return quote + s + quote;
      }
    }

    return s;
  }

  /**
   * Skips the given string in the given reader, if the given reader's remaining input starts with
   * the given string.
   *
   * @param reader The reader to move the cursor of
   * @param s      The string to skip
   */
  public static void skip(StringReader reader, String s) {
    if (!Readers.startsWithIgnoreCase(reader, s)) {
      return;
    }

    reader.setCursor(reader.getCursor() + s.length());
  }

  /**
   * Ensures that the given string reader is at the end of its input
   *
   * @param reader The reader to test
   * @throws CommandSyntaxException If the reader is not at the end of it's input
   */
  public static void ensureCannotRead(StringReader reader) throws CommandSyntaxException {
    if (reader.canRead()) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .dispatcherUnknownArgument()
          .createWithContext(reader);
    }
  }

  /**
   * Executes a specified command as the server's console
   * @param format The command format
   * @param args Arguments to format
   */
  public static void executeConsole(String format, Object... args) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format(format, args));
  }
}