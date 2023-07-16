package net.forthecrown.text;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;

import javax.annotation.Nullable;
import net.forthecrown.Worlds;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;

public interface Messages {

  Style CHAT_URL = Style.style(TextDecoration.UNDERLINED)
      .hoverEvent(text("Click to open link!"));

  TextComponent FTC_PREFIX = Text.gradient("[FTC] ", NamedTextColor.BLACK, NamedTextColor.GOLD);

  /**
   * Common text which simply states "Click to allow"
   */
  TextComponent CLICK_TO_ALLOW = text("Click to Allow");

  /**
   * Common text which simply states "Click to deny"
   */
  TextComponent CLICK_TO_DENY = text("Click to Deny");

  /**
   * Common text which simply states "Click to confirm" Used mostly in hover events
   */
  TextComponent CLICK_TO_CONFIRM = text("Click to confirm");

  /**
   * A tick encased by square brackets with {@link #CLICK_TO_ALLOW} hover event
   */
  TextComponent BUTTON_ACCEPT_TICK = text("[✔]").hoverEvent(CLICK_TO_ALLOW);

  /**
   * A special unicode cross encased by square brackets with {@link #CLICK_TO_DENY} hover event.
   */
  TextComponent BUTTON_DENY_CROSS = text("[✖]").hoverEvent(CLICK_TO_DENY);

  /**
   * Standard " < " previous page button with hover text, and bold and yellow styling
   */
  TextComponent PREVIOUS_PAGE = text(" < ", NamedTextColor.YELLOW, TextDecoration.BOLD)
      .hoverEvent(translatable("spectatorMenu.previous_page"));

  /**
   * Standard " > " next page button with hover text, and bold and yellow styling
   */
  TextComponent NEXT_PAGE = text(" > ", NamedTextColor.YELLOW, TextDecoration.BOLD)
      .hoverEvent(translatable("spectatorMenu.next_page"));

  /**
   * A standard page border made up of spaces with a {@link TextDecoration#STRIKETHROUGH} style
   * applied to them as well as gray coloring.
   */
  TextComponent PAGE_BORDER = text("                  ",
      NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH
  );

  /**
   * Standard aqua colored button which states "[Confirm]" and has {@link #CLICK_TO_CONFIRM} as the
   * hover text
   */
  TextComponent BUTTON_CONFIRM = text("[Confirm]", NamedTextColor.AQUA)
      .hoverEvent(CLICK_TO_CONFIRM);

  /**
   * Red button which states "[Deny]" and has {@link #CLICK_TO_DENY} as the hover text
   */
  TextComponent BUTTON_DENY = text("[Deny]", NamedTextColor.RED)
      .hoverEvent(CLICK_TO_DENY);

  /**
   * Green button which states "[Acccept]" and has "Click to accept" as the hover text
   */
  TextComponent BUTTON_ACCEPT = text("[Accept]", NamedTextColor.GREEN)
      .hoverEvent(text("Click to accept"));

  /**
   * Common text which says "Click me!" lol
   */
  TextComponent CLICK_ME = text("Click me!");

  /**
   * Uncategorized text which says that special items cannot be dropped
   */
  TextComponent CANNOT_DROP_SPECIAL = text("Cannot drop special item!", NamedTextColor.RED);

  /**
   * Uncategorized message which states that all-caps messages cannot be sent
   */
  TextComponent ALL_CAPS = text("Please do not send all caps messages.", NamedTextColor.GRAY);

  /**
   * Uncategorized display name of the "Hit me!" dummy entities
   */
  TextComponent DUMMY_NAME = text("Hit me!", NamedTextColor.GOLD);

  /**
   * Red ❤
   */
  TextComponent HEART = text("❤", NamedTextColor.DARK_RED);

  /**
   * Cyan claim button for mail
   */
  TextComponent CLAIM = text("[Claim]", NamedTextColor.AQUA);

  /**
   * Message shown when denying an incoming request of any kind.
   */
  TextComponent REQUEST_DENIED = text("Request denied.", NamedTextColor.GRAY);

  /**
   * Message shown when accepting an incoming request of any kind.
   */
  TextComponent REQUEST_ACCEPTED = text("Accepted request.", NamedTextColor.GRAY);

  /**
   * Message shown when cancelling an outgoing request of any kind
   */
  TextComponent REQUEST_CANCELLED = text("Cancelling request.", NamedTextColor.YELLOW);

  /**
   * Message stating something was claimed
   */
  TextComponent CLAIMED = text("Claimed ", NamedTextColor.YELLOW);

  /**
   * This is for a weird thing to get around an issue, there are some commands that accept '-clear'
   * as a valid input for a message to clear some text or line somewhere, this allows us to still
   * use a message argument while testing if the input is meant to be a clear command or not
   */
  TextComponent DASH_CLEAR = text("-clear");

  /**
   * Text informing the viewer of lacking permissions
   */
  TextComponent NO_PERMISSION = text("You do not have permission to use this!", NamedTextColor.RED);

  /**
   * Text which simply says null
   */
  TextComponent NULL = text("null");

  /**
   * Unknown command message that's used as a permission denied message
   */
  TextComponent UNKNOWN_COMMAND
      = text("Unknown command. Type\"/help\" for help", NamedTextColor.WHITE);

  TextComponent NOTHING_CHANGED = text("Nothing changed", NamedTextColor.GRAY);

  TextColor CHAT_NAME_COLOR = TextColor.fromHexString("#e6e6e6");

  /**
   * Format used by {@link Users#testBlocked(User, User, String, String)} for separated players.
   */
  String SEPARATED_FORMAT = "You are forcefully separated from {0, user}!";

  String BLOCKED_SENDER = "You have blocked {0, user}!";

  String BLOCKED_TARGET = "{0, user} has blocked you!";

  String BASE_JOIN_MESSAGE = "{0, user} joined the game";

  String BASE_JOIN_MESSAGE_NEW_NAME = "{0, user} (formerly known as {1}) joined the game";

  String BASE_LEAVE_MESSAGE = "{0, user} left the game";
  String BASE_LEAVE_MESSAGE_TIMEOUT = "{0, user} left the game (Timed out)";
  String BASE_LEAVE_MESSAGE_ERROR = "{0, user} left the game (Error)";
  String BASE_LEAVE_MESSAGE_KICKED = "{0, user} left the game (Kicked)";

  static Component createButton(Component text, String cmd, Object... args) {
    return text.clickEvent(runCommand(String.format(cmd, args)));
  }

  /**
   * Creates a message which states that the given user is not online
   *
   * @param user The user whose display name to use
   * @return The formatted message
   */
  static Component notOnline(User user) {
    return format("{0, user} is not online", user);
  }

  /**
   * Creates a next page button by applying the given click event to the {@link #NEXT_PAGE}
   * constant.
   *
   * @param event The click event to apply, may be null
   * @return The created text
   */
  static Component nextPage(@Nullable ClickEvent event) {
    return NEXT_PAGE.clickEvent(event);
  }

  /**
   * Creates a previous page button by applying the given click event to the {@link #PREVIOUS_PAGE}
   * constant.
   *
   * @param event The click event to apply, may be null
   * @return The created text
   */
  static Component previousPage(@Nullable ClickEvent event) {
    return PREVIOUS_PAGE.clickEvent(event);
  }

  /**
   * Returns {@link #BUTTON_ACCEPT_TICK} with the given <code>cmd</code> as the
   * <code>run_command</code> click event
   *
   * @param cmd The command to use
   * @return The created button component
   */
  static Component tickButton(String cmd, Object... args) {
    return createButton(BUTTON_ACCEPT_TICK, cmd, args);
  }

  /**
   * Returns {@link #BUTTON_DENY_CROSS} with the given <code>cmd</code> as the
   * <code>run_command</code> click event
   *
   * @param cmd The command to use
   * @return The created button component
   */
  static Component crossButton(String cmd, Object... args) {
    return createButton(BUTTON_DENY_CROSS, cmd, args);
  }

  /**
   * Returns {@link #BUTTON_CONFIRM} with the given
   * <code>cmd</code> as the <code>run_command</code> click event
   *
   * @param cmd The command to use
   * @return The created button
   */
  static Component confirmButton(String cmd, Object... args) {
    return createButton(BUTTON_CONFIRM, cmd, args);
  }

  /**
   * Returns {@link #BUTTON_DENY} with the given
   * <code>cmd</code> as the <code>run_command</code> click event
   *
   * @param cmd The command to use
   * @return The created button
   */
  static Component denyButton(String cmd, Object... args) {
    return createButton(BUTTON_DENY, cmd, args);
  }

  /**
   * Returns {@link #BUTTON_ACCEPT} with the given
   * <code>cmd</code> as the <code>run_command</code> click event
   *
   * @param cmd The command to use
   * @return The created button
   */
  static Component acceptButton(String cmd, Object... args) {
    return createButton(BUTTON_ACCEPT, cmd, args);
  }

  /**
   * Creates a message stating the viewer died at the given location
   *
   * @param l The location the viewer died at
   * @return The formatted message
   */
  static Component diedAt(Location l) {
    return format("You died at &e{0, location}&r{1}",
        NamedTextColor.GRAY,
        l,

        // Optionally add world name, only if
        // not in overworld
        l.getWorld().equals(Worlds.overworld())
            ? "!"
            : "world: " + Text.formatWorldName(l.getWorld()) + "!"
    );
  }

  static Component chatMessage(Component displayName, Component message) {
    return format("{0} &7&l> &r{1}",
        displayName.color(CHAT_NAME_COLOR),
        message
    );
  }

  /**
   * Creates a message sent to the sender of a request.
   *
   * @param target The target of the request
   * @return The formatted message
   */
  static Component requestSent(User target, Component cancelButton) {
    return format("Sent request to &e{0, user}&r. &7{1}",
        NamedTextColor.GOLD,
        target,
        cancelButton
    );
  }


  /**
   * Creates an accept message for the request's sender telling them that the request's target has
   * accepted the request.
   *
   * @param target The Target that accepted the request
   * @return The formatted message
   */
  static Component requestAccepted(User target) {
    return format("&e{0, user}&r accepted your request.",
        NamedTextColor.GOLD, target
    );
  }

  /**
   * Creates a denied message for the request's sender informing them that the request's target has
   * denied the request.
   *
   * @param target The user that denied the request
   * @return The formatted message
   */
  static Component requestDenied(User target) {
    return format("&6{0, user}&r denied your request.",
        NamedTextColor.GRAY, target
    );
  }

  /**
   * Creates a cancellation message to tell the request's target that the sender cancelled the
   * request.
   *
   * @param sender The user that cancelled the request
   * @return The formatted message
   */
  static Component requestCancelled(User sender) {
    return requestCancelled(sender.displayName());
  }

  static Component requestCancelled(Component name) {
    return format("&6{0}&r cancelled their request",
        NamedTextColor.GRAY, name
    );
  }

  /**
   * Formats the message to tell users they have enabled/disabled a boolean
   * {@link net.forthecrown.user.UserProperty}
   * <p>
   * The format is given 2 of the following arguments, which changes depending
   * on the <code>state</code> parameter:<pre>
   * Argument 0: "Enabled" or "Disabled"
   * Argument 1: "ow" or "o longer"
   * Argument 2: "o longer" or "ow", same as above basically, but inverse
   * </pre>
   * The second argument doesn't have a starting 'n', this is so you can decide if that letter
   * should be capitalized yourself.
   * <p>
   * The color of the returned text also depends on the <code>state</code> parameter, if it's true,
   * the color will be yellow, otherwise it'll be gray
   *
   * @param format The message format to use.
   * @param state  The new state of the property
   * @return The formatted component
   */
  static Component toggleMessage(String format, boolean state) {
    return format(format,
        state ? NamedTextColor.YELLOW : NamedTextColor.GRAY,
        /* Arg 0 */ state ? "Enabled" : "Disabled",
        /* Arg 1 */ state ? "ow" : "o longer",
        /* Arg 2 */ state ? "o longer" : "ow"
    );
  }

  /**
   * Creates a message for staff when they change a boolean
   * {@link net.forthecrown.user.UserProperty} for another user.
   *
   * @param display The setting's display name
   * @param user    The user the value was changed for
   * @param state   The new state of the property
   * @return The formatted component
   */
  static Component toggleOther(String display, User user, boolean state) {
    return format("{0} {1} for &e{2, user}",
        NamedTextColor.GRAY,

        !state ? "Disabled" : "Enabled",
        display, user
    );
  }

  static Component joinMessage(Component displayName) {
    return Text.format(BASE_JOIN_MESSAGE, NamedTextColor.YELLOW, displayName);
  }

  static Component newNameJoinMessage(Component displayName, Object previousName) {
    return Text.format(BASE_JOIN_MESSAGE_NEW_NAME, NamedTextColor.YELLOW,
        displayName, previousName
    );
  }

  static Component leaveMessage(Component displayName, QuitReason reason) {
    String format = switch (reason) {
      case KICKED -> BASE_LEAVE_MESSAGE_KICKED;
      case TIMED_OUT -> BASE_LEAVE_MESSAGE_TIMEOUT;
      case ERRONEOUS_STATE -> BASE_LEAVE_MESSAGE_ERROR;
      default -> BASE_LEAVE_MESSAGE;
    };
    
    return Text.format(format, NamedTextColor.YELLOW, displayName);
  }


  /**
   * Creates a message stating the user has the given text
   *
   * @param unitDisplay The unit display of the user
   * @return The formatted message
   */
  static Component unitQuerySelf(Component unitDisplay) {
    return format("You have &e{0}&r.",
        NamedTextColor.GRAY, unitDisplay
    );
  }

  /**
   * Creates a message stating the given user has the given text.
   *
   * @param unitDisplay The unit display of the user
   * @param target      The user
   * @return The formatted message
   */
  static Component unitQueryOther(Component unitDisplay, User target) {
    return format("&e{0, user} &rhas &6{1}&r.",
        NamedTextColor.GRAY,
        target, unitDisplay
    );
  }
}