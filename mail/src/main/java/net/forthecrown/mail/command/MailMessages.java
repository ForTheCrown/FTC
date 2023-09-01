package net.forthecrown.mail.command;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

interface MailMessages {


  /**
   * The mail received message to display for received mail that has no sender
   */
  TextComponent MAIL_RECEIVED = text("You've got mail!", NamedTextColor.GOLD);

  /**
   * Header used for the mail list, if the mail being viewed is the user's own
   */
  TextComponent MAIL_HEADER_SELF = text("Your mail", NamedTextColor.GOLD);

  /**
   * Gray "[Unread]" button, with "Mark as unread" as the hover text
   */
  TextComponent MAIL_UNREAD = text("[Unread]", NamedTextColor.GRAY)
      .hoverEvent(text("Mark as unread"));

  /**
   * Yellow "[Read]" button, with "Mark as read" as the hover text
   */
  TextComponent MAIL_READ = text("[Read]", NamedTextColor.YELLOW)
      .hoverEvent(text("Mark as read"));

  /**
   * Attachment button, not for actually claiming the attachment, but for displaying it if anyone
   * other than the owner of the mail is viewing the mail list
   */
  TextComponent MAIL_ATTACHMENT = text("[Attachment]", NamedTextColor.GRAY);

  /**
   * Yellow message stating a message was marked as read
   */
  TextComponent MARKED_READ = text("Marked as read", NamedTextColor.YELLOW);

  /**
   * Gray message stating a message was marked as unread
   */
  TextComponent MARKED_UNREAD = text("Marked as unread", NamedTextColor.GRAY);

  TextComponent MAIL_CLEARED = text("Mail cleared", NamedTextColor.GRAY);

  /**
   * Creates a "You have mail" message for a user to inform them that they've received mail while
   * they were offline.
   *
   * @param unreadAmount The amount of unread messages the user has received
   * @return The formatted message
   */
  static Component mailJoinMessage(int unreadAmount) {
    return format(
        "You have mail! &7{0} unread messages.",
        NamedTextColor.YELLOW,
        unreadAmount
    )
        .clickEvent(ClickEvent.runCommand("/mail"))
        .hoverEvent(text("Click to read mail!"));
  }

  /**
   * Creates a message to tell a user they sent a mail message to the given user
   *
   * @param target The target of the mail
   * @param text   The mail's content
   * @return The formatted message
   */
  static Component mailSent(User target, Component text) {
    return format("Sent mail to &6{0, user}&r: '{1}'",
        NamedTextColor.YELLOW,
        target, text.color(NamedTextColor.WHITE)
    );
  }

  /**
   * Creates a mail list header with the given user's display name
   *
   * @param user The user whose display name to use
   * @return The formatted message
   */
  static Component mailHeader(User user) {
    return format("{0, user}'s mail", user);
  }

  /**
   * Creates a message stating the given user's mail was cleared
   *
   * @param target The user whose mail was cleared
   * @return The formatted message
   */
  static Component clearedMail(User target) {
    return format("Cleared &e{0, user}&r's mail",
        NamedTextColor.GRAY, target
    );
  }

  /**
   * Creates a message stating a message was marked as read/unread for the given user
   *
   * @param target The user the message's status was changed for
   * @param read   True, if the message was marked as read, false for unread
   * @return The formatted message
   */
  static Component markedReadOther(User target, boolean read) {
    return format("Marked a message as {0} for {1, user}",
        read ? NamedTextColor.YELLOW : NamedTextColor.GRAY,
        read ? "read" : "unread",
        target
    );
  }
}
