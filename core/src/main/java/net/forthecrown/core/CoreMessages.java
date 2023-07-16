package net.forthecrown.core;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import java.util.Collection;
import java.util.UUID;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;

public interface CoreMessages {


  /**
   * The title header of the ignored players list
   */
  Component IGNORE_LIST_HEADER = text("Ignored players: ", NamedTextColor.GOLD);

  Component GAME_MODE_SELF = text("own", NamedTextColor.GOLD);

  /**
   * Lists all blocked users
   *
   * @param users The users to list
   * @return The formatted component
   */
  static Component listBlocked(Collection<UUID> users) {
    return joinIds(users, IGNORE_LIST_HEADER);
  }

  static Component joinIds(Collection<UUID> uuids, Component header) {
    return TextJoiner.onComma()
        .setColor(NamedTextColor.GOLD)
        .setPrefix(header)
        .add(uuids.stream().map(uuid -> {
          var user = Users.get(uuid);
          return user.displayName().color(NamedTextColor.YELLOW);
        }))
        .asComponent();
  }


  /**
   * Creates a message saying the given player was ignored
   *
   * @param target The player being ignored
   * @return The formatted message
   */
  static Component ignorePlayer(User target) {
    return format("Ignored &6{0, user}", NamedTextColor.YELLOW, target);
  }

  /**
   * Creates a message saying the given player was unignored
   *
   * @param target The player being unignored
   * @return The formatted message
   */
  static Component unignorePlayer(User target) {
    return format("Unignored &e{0, user}", NamedTextColor.GRAY, target);
  }

  static Component gameModeChangedSelf(GameMode mode) {
    return format("Set {0} gamemode to &e{1}&r.",
        NamedTextColor.GRAY,
        GAME_MODE_SELF,
        translatable(mode)
    );
  }

  static Component gameModeChangedOther(User target, GameMode mode) {
    return format("Set {0, user}'s gamemode to &e{1}&r.",
        NamedTextColor.GRAY,
        target,
        translatable(mode)
    );
  }

  static Component gameModeChangedTarget(CommandSource changer, GameMode mode) {
    return format("&6{0}&r changed your gamemode to &e{1}&r.",
        NamedTextColor.GRAY,
        Text.sourceDisplayName(changer),
        translatable(mode)
    );
  }

  static Component sudoCommand(User target, String cmd) {
    return format("Forcing &e{0, user}&r to run '&f{1}&r'",
        NamedTextColor.GRAY,
        target, cmd
    );
  }

  static Component sudoChat(User target, String chat) {
    return format("Forcing &e{0, user}&r to say '&f{1}&r'",
        NamedTextColor.GRAY,
        target, chat
    );
  }
}
