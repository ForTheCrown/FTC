package net.forthecrown.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.StackWalker.Option;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.ExpandedEntityArgument;
import net.forthecrown.command.arguments.UserParseResult;
import net.forthecrown.command.help.FtcSyntaxConsumer;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext.DefaultExecutionRule;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.CommandDataLoader;
import net.forthecrown.grenadier.annotations.TypeRegistry;
import net.forthecrown.text.page.PageEntryIterator;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import net.minecraft.server.commands.FunctionCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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

    ArgumentModifier<UserParseResult, User> resultToUser = (context, input) -> {
      return input.get(context.getSource(), true);
    };
    ctx.getVariables().put("result_to_user", resultToUser);

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

  public static String getDefaultPermission(String commandName) {
    return DEFAULT_PERMISSION_FORMAT.replace("{command}", commandName);
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
   * Gets the specified {@code source} as a {@link User}
   * @param source Command source
   * @return Source's user object
   * @throws CommandSyntaxException If the {@code source} is not a player
   */
  public static User getUserSender(CommandSource source) throws CommandSyntaxException {
    return Users.get(source.asPlayer());
  }

  /**
   * Executes a specified command as the server's console
   * @param format The command format
   * @param args Arguments to format
   */
  public static void executeConsole(String format, Object... args) {
    String formattedCmd = String.format(format, args);

    if (Bukkit.isPrimaryThread()) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
    } else {
      Plugin plugin = PluginUtil.getCallingPlugin();
      Bukkit.getScheduler().runTask(plugin, () -> {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
      });
    }
  }
}