package net.forthecrown.antigrief;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import net.forthecrown.user.User;

public interface GMessages {


  /**
   * Message shown to hard muted users when they attempt speak
   */
  Component YOU_ARE_MUTED = text("You are muted!", NamedTextColor.RED);

  /**
   * Message saying that commands cannot be used while jailed
   */
  Component JAIL_NO_COMMANDS = text("Cannot use commands while jailed",
      NamedTextColor.RED
  );

  /**
   * Message used to warn users when they fail
   * {@link net.forthecrown.core.admin.BannedWords#checkAndWarn(CommandSender, Component)}
   */
  Component BAD_LANGUAGE = text("Mind your tongue, you wench", NamedTextColor.RED);

  /**
   * Formats a message stating the users are being separated
   *
   * @param first  The first user
   * @param second The second user
   * @return the formatted message
   */
  static Component separating(User first, User second) {
    return format("Separating {0, user} and {1, user}",
        first, second
    );
  }

  /**
   * Formats a message stating the two given users are being unseparated
   *
   * @param first  The first user
   * @param second The second user
   * @return The formatted message
   */
  static Component unseparating(User first, User second) {
    return format("Unseparating {0, user} and {1, user}",
        first, second
    );
  }

}