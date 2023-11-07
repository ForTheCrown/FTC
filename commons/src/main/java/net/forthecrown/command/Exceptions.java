package net.forthecrown.command;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.SyntaxExceptions;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;

/**
 * Factory class for creating exceptions.
 * <p>
 * Exceptions which have no dynamic aspects, ie, they are a single message that requires no command
 * context, should be stored as exception constants
 */
public interface Exceptions {

  // ------------------------------------------------
  // --- SECTION: EXCEPTION FACTORIES / UTILITIES ---
  // ------------------------------------------------

  static CommandSyntaxException create(ComponentLike component) {
    return Grenadier.exceptions().create(component.asComponent());
  }

  /**
   * Creates an exception with the given message
   *
   * @param message The message to create an exception with
   * @return The created exception
   */
  static CommandSyntaxException create(String message) {
    return create(Text.renderString(message));
  }

  /**
   * Creates an exception by formatting the given format with the given arguments
   *
   * @param format The message format to use
   * @param args   The args to format with
   * @return The created exception
   * @see Text#format(Component, Object...)
   */
  static CommandSyntaxException format(String format, Object... args) {
    return create(Text.format(format, args));
  }

  /**
   * Creates an exception by formatting the given message format with the given arguments.
   *
   * @param format The message format to use
   * @param color  The color to apply onto the format
   * @param args   The arguments to format with
   * @return The created exception
   * @see Text#format(Component, Object...)
   */
  private static CommandSyntaxException format(String format, TextColor color, Object... args) {
    return create(Text.format(format, color, args));
  }

  /**
   * Creates an exception by formatting the given message format with the given arguments.
   *
   * @param format The message format to use
   * @param reader The reader to use for the exception's context
   * @param args   The arguments to format with
   * @return The created exception
   * @see Text#format(Component, Object...)
   */
  static CommandSyntaxException formatWithContext(String format,
                                                  ImmutableStringReader reader,
                                                  Object... args
  ) {
    return Grenadier.exceptions().createWithContext(
        Text.format(format, args).asComponent(),
        reader
    );
  }

  /**
   * Handles the given {@link CommandSyntaxException} by formatting it and sending it to the given
   * <code>sender</code>
   *
   * @param sender    The sender to send the formatted message to
   * @param exception The exception to format
   */
  static void handleSyntaxException(Audience sender, CommandSyntaxException exception) {
    if (sender instanceof CommandSource source) {
      SyntaxExceptions.handle(exception, source);
      return;
    }

    sender.sendMessage(SyntaxExceptions.formatCommandException(exception));
  }

  // ---------------------------------------
  // --- SECTION: COMMON / UNCATEGORIZED ---
  // ---------------------------------------

  /**
   * Exception which states your inventory is full
   */
  CommandSyntaxException INVENTORY_FULL = create("Your inventory is full.");

  CommandSyntaxException NOT_ENOUGH_ROOM = create("Not enough space in your inventory");

  CommandSyntaxException INVENTORY_EMPTY = create("Your inventory is empty.");

  /**
   * Exception which states you must be holding any kind of item
   */
  CommandSyntaxException MUST_HOLD_ITEM = create("You must be holding an item.");

  /**
   * Exception which states there's nothing to list. This is intentionally really vague as to allow
   * for maximum amount of usability
   */
  CommandSyntaxException NOTHING_TO_LIST = create("Nothing to list!");

  CommandSyntaxException NO_REGION_SELECTION = create("No region selection (//wand selection)");

  CommandSyntaxException NO_PERMISSION = create("You do not have permission to do this");

  CommandSyntaxException NOTHING_CHANGED = create("Nothing changed");

  CommandSyntaxException NOT_UNLOCKED = create("You haven't unlocked this yet.");

  /**
   * Creates an exception which says the given user is not online
   *
   * @param user The user that isn't online
   * @return The created exception
   */
  static CommandSyntaxException notOnline(User user) {
    return format("{0, user} is not online", user);
  }

  /**
   * A generic exception factory.
   * <p>
   * This will created a "Missing name" message with the given context, the first name parameter is
   * the missing object's type
   *
   * @param name    The type name
   * @param reader  The context of the exception
   * @param missing The key of the unknown value
   * @return The created exception
   */
  static CommandSyntaxException unknown(String name, ImmutableStringReader reader, String missing) {
    return formatWithContext("Unknown {0}: '{1}'", reader, name, missing);
  }

  /**
   * Creates an exception which states that the given index is not valid for the max size
   *
   * @param index The index
   * @param max   The maximum index value
   * @return The created exception
   */
  static CommandSyntaxException invalidIndex(int index, int max) {
    return format("Invalid index: {0, number}, max: {1, number}", index, max);
  }

  /**
   * Creates an exception which states that the given page is invalid
   *
   * @param page    The page
   * @param maxPage The max page
   * @return The created exception
   */
  static CommandSyntaxException invalidPage(int page, int maxPage) {
    return format("Invalid page: {0, number}, max: {1, number}", page, maxPage);
  }

  /**
   * Creates an exception stating the viewer can only perform an action every <code>millis</code>
   * delay.
   *
   * @param millis The millis cooldown length
   * @return The created exception
   */
  static CommandSyntaxException onCooldown(long millis) {
    return format("You can only do this every: {0, time}", millis);
  }

  static CommandSyntaxException cooldownEndsIn(long millis) {
    return format("You can do this again in {0, time}", millis);
  }

  static CommandSyntaxException alreadyExists(String name, Object value) {
    return format("{0} named '{1}' already exists",
        name, value
    );
  }

  static CommandSyntaxException noIncoming(User user) {
    return format("You haven't received any requests from {0, user}.", user);
  }

  static CommandSyntaxException noOutgoing(User user) {
    return format("You haven't sent any requests to {0, user}.", user);
  }

  static CommandSyntaxException requestAlreadySent(User target) {
    return format("You've already sent a request to {0, user}.", target);
  }

  static CommandSyntaxException invalidBounds(int start, int end) {
    return format("Invalid bounds! Min ({0}) was larger than Max ({1})",
        start, end
    );
  }

  static CommandSyntaxException unknownUser(StringReader reader, String name) {
    return unknown("user", reader, name);
  }


  /**
   * Creates an exception which states the user cannot afford the given amount of Rhines
   *
   * @param amount The amount the user cannot afford
   * @return The created exception
   */
  static CommandSyntaxException cannotAfford(Number amount) {
    return format("Cannot afford {0, rhines}", amount);
  }
}