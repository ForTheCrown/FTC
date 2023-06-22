package net.forthecrown.antigrief;

import static net.forthecrown.command.Exceptions.format;
import static net.forthecrown.command.Exceptions.create;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.PunishType;
import net.forthecrown.user.User;

public interface GExceptions {

  /**
   * Exception stating an offline player cannot be kicked
   * <p>
   * Used by {@link net.forthecrown.antigrief.commands.PunishmentCommand.CommandKick}
   */
  CommandSyntaxException CANNOT_KICK_OFFLINE = create("Cannot kick offline player");

  /**
   * Exception stating that a given jail spawn position is invalid due to it being outside the
   * selected jail-cell area.
   * <p>
   * Used by {@link net.forthecrown.antigrief.commands.CommandJails}
   */
  CommandSyntaxException INVALID_JAIL_SPAWN = create(
      "Jail spawn point (The place you're standing at) " +
          "isn't inside the cell room"
  );

  /**
   * Creates an exception stating the given user cannot be punished.
   * <p>
   * This will most likely be thrown because a staff member attempted to use a punishment on a staff
   * member of higher rank.
   * <p>
   * Used by {@link net.forthecrown.antigrief.commands.PunishmentCommand}
   *
   * @param user The user that cannot be punished
   * @return The created exception
   */
  static CommandSyntaxException cannotPunish(User user) {
    return format("Cannot punish: {0, user}", user);
  }

  /**
   * Creates an exception stating the given user has already received the given type of punishment
   * <p>
   * Used by {@link net.forthecrown.antigrief.commands.PunishmentCommand}
   *
   * @param user The user that has been punished
   * @param type The punishment
   * @return The created exception
   */
  static CommandSyntaxException alreadyPunished(User user, PunishType type) {
    return format("{0, user} has already been {1}!", user, type.nameEndingED());
  }

  /**
   * Creates an exception stating that a jail with the given
   * <code>key</code> already exists and a new one cannot be
   * created
   * <p>
   * Used by {@link net.forthecrown.antigrief.commands.CommandJails}
   *
   * @param key The name of the jail cell
   * @return The created exception
   */
  static CommandSyntaxException jailExists(String key) {
    return format("Jail named '{0, key}' already exists", key);
  }

  static CommandSyntaxException noNotes(PunishEntry entry) {
    return format("{0, user} has no staff notes", entry.getHolder());
  }

  static CommandSyntaxException notPunished(User user, PunishType type) {
    return format("{0, user} is not {1}",
        user, type.nameEndingED()
    );
  }

  static CommandSyntaxException cannotPardon(PunishType type) {
    return format("You do not have enough permissions to pardon a {0}",
        type.presentableName()
    );
  }
}