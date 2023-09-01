package net.forthecrown.marriages;

import static net.forthecrown.text.Messages.CLICK_ME;
import static net.forthecrown.text.Messages.acceptButton;
import static net.forthecrown.text.Messages.confirmButton;
import static net.forthecrown.text.Messages.denyButton;
import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.format.FormatBuilder;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public interface MMessages {

  /**
   * The "[Marriage]" prefix that is prepended onto th marriage message before the sender's display
   * name
   */
  TextComponent MARRIAGE_PREFIX = text()
      .color(TextColor.color(255, 158, 208))
      .append(
          text("["),
          text("Marriage", TextColor.color(255, 204, 230)),
          text("] ")
      )
      .build();

  /**
   * The pink header/message delimiter pointer: " > "
   */
  TextComponent MARRIAGE_POINTER = text(" > ", TextColor.color(255, 158, 208), TextDecoration.BOLD);


  /**
   * Message stating the viewer cannot send a marriage chat message
   */
  TextComponent CANNOT_SEND_MCHAT = text("Cannot send Marriage Chat message", NamedTextColor.GRAY);


  Component HAZELGUARD_CHAPEL = text("The chapel in Hazelguard", NamedTextColor.YELLOW);

  Component PRIEST_TEXT_CONFIRM = text("[I do]", NamedTextColor.AQUA).hoverEvent(CLICK_ME);

  Component PRIEST_TEXT_WAITING = text("Now your spouse will have to accept.",
      NamedTextColor.YELLOW
  );

  Component PRIEST_TEXT_MARRY = text("[Marry someone]", NamedTextColor.AQUA).hoverEvent(CLICK_ME);

  Component PRIEST_TEXT = text("Welcome to the church. What can I help you with?",
      NamedTextColor.YELLOW
  );

  static Component proposeSender(User target) {
    return format("Proposed to &e{0, user}&r.", NamedTextColor.GOLD, target);
  }

  static Component proposeTarget(User sender) {
    return format("&e{0, user}&r has proposed to you! {1} {2}",
        NamedTextColor.GOLD,
        sender,
        acceptButton("/marryaccept " + sender.getName()),
        denyButton("/marrydeny")
    );
  }

  static Component confirmDivorce(User spouse) {
    return format("Are you sure you wish to divorce &e{0, user}&r? {1}",
        NamedTextColor.GRAY,
        spouse,
        confirmButton("/divorce confirm")
    );
  }

  static Component proposeDenySender(User target) {
    return format("&e{0, user}&r denied your proposal :(",
        NamedTextColor.GRAY, target
    );
  }

  static Component proposeDenyTarget(User sender) {
    return format("Denied &e{0, user}&r's proposal.",
        NamedTextColor.GRAY, sender
    );
  }

  static Component proposeAcceptSender() {
    return format("Accepted marriage proposal! Hurry to {0} to complete the ritual!",
        NamedTextColor.GRAY, HAZELGUARD_CHAPEL
    );
  }

  static Component proposeAcceptTarget(User sender) {
    return format(
        "&e{0, user}&r accepted your marriage proposal! Hurry to {1} to complete the ritual!",
        NamedTextColor.GRAY, sender, HAZELGUARD_CHAPEL
    );
  }

  static Component priestTextConfirm(User sender, User target) {
    return format("Do you, &6{0, user}&r, take &6{1, user}&r to be your lawfully wed spouse?",
        NamedTextColor.YELLOW,
        sender, target
    );
  }

  /**
   * Creates a message that says you are now married to the given user
   *
   * @param to The user who the viewer of this message is married to
   * @return The formatted message
   */
  static ViewerAwareMessage nowMarried(User to) {
    return FormatBuilder.builder()
        .setFormat("You are now married to &6{0, user}&r!", NamedTextColor.GOLD)
        .setArguments(to)
        .asViewerAware();
  }

  /**
   * Creates a message stating the viewer divorced the given user
   *
   * @param spouse The viewer's now ex-spouse
   * @return The formatted message
   */
  static ViewerAwareMessage senderDivorced(User spouse) {
    return FormatBuilder.builder()
        .setFormat("Divorced &e{0, user}&r.", NamedTextColor.GOLD)
        .setArguments(spouse)
        .asViewerAware();
  }

  /**
   * Creates a message saying the given user has divorced the viewer of the message
   *
   * @param user The user that divorced the viewer
   * @return The formatted message
   */
  static Component targetDivorced(User user) {
    return format("&6{0, user}&e divorced you.", user);
  }
}
