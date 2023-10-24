package net.forthecrown.core;

import static net.forthecrown.text.Text.format;
import static net.forthecrown.text.Text.isEmpty;
import static net.forthecrown.text.Text.vformat;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import net.forthecrown.core.user.UserHomes;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;

public interface CoreMessages {

  /**
   * The title header of the ignored players list
   */
  Component IGNORE_LIST_HEADER = text("Ignored players: ", NamedTextColor.GOLD);

  Component GAME_MODE_SELF = text("own", NamedTextColor.GOLD);

  /**
   * Suffix to display for players on the <code>/list</code> command that are hidden, aka,
   * vanished.
   */
  Component VANISHED_LIST_SUFFIX = text(" [Hidden]", NamedTextColor.GRAY);

  /**
   * Header for the <code>/near</code> command
   */
  Component NEARBY_HEADER = text("Nearby players: ", NamedTextColor.GOLD);

  /**
   * Message shown by {@link net.forthecrown.core.commands.CommandSuicide}
   */
  Component CMD_SUICIDE = text("Committing suicide :(", NamedTextColor.GRAY);

  /**
   * Message stating the viewer was healed
   */
  TextComponent HEALED = text("You were healed!", NamedTextColor.YELLOW);

  /**
   * Message stating the viewer had their appetite satiated.
   */
  TextComponent FED = text("You were fed!", NamedTextColor.YELLOW);

  /**
   * Inventory title used by {@link net.forthecrown.core.commands.CommandSelfOrUser} for it's
   * <code>/bin</code> command
   */
  Component DISPOSAL = text("Disposal");

  Component CLEARED_ENCHANTMENTS = text("Cleared all enchantments", NamedTextColor.GRAY);

  Component CLEARED_LORE = text("Cleared lore", NamedTextColor.GRAY);

  Component CLEARED_ITEM_NAME = text("Cleared item name!", NamedTextColor.GRAY);

  Component MERGED_ITEM_DATA = text("Merged item data", NamedTextColor.GRAY);

  Component REMOVED_ITEM_DATA = text("Removed item data", NamedTextColor.GRAY);

  Component CLEARED_ATTRIBUTE_MODS = text("Cleared attribute modifiers", NamedTextColor.GRAY);

  Component REMOVED_ATTRIBUTE_MOD = text("Removed attribute modifier", NamedTextColor.GRAY);

  String WITHDRAW_FORMAT_SINGLE = "You got &e{0}&r that's worth &6{1}&r.";

  String WITHDRAW_FORMAT_MULTIPLE = "You got &e{0}&r that are worth &6{1}&r.";


  /**
   * Message stating that a default home was set
   */
  TextComponent HOMES_DEF_SET = text("Default home set", NamedTextColor.GOLD);

  /**
   * Message stating that you're teleporting home
   */
  TextComponent TELEPORTING_HOME = text("Teleporting home", NamedTextColor.GRAY);

  /**
   * Home list header for when you're viewing your own homes
   */
  TextComponent HOMES_LIST_HEADER_SELF = text("Your homes", NamedTextColor.GOLD);

  /**
   * Creates a message stating that a home by the given name was set to the viewer's current
   * location
   *
   * @param name The name of the home that was set
   * @return The formatted message
   */
  static Component homeSet(String name) {
    return format("Set home &6{0}&r to current location",
        NamedTextColor.YELLOW, name
    );
  }

  /**
   * Creates a message stating the user is teleporting to a home by the given name.
   *
   * @param homeName The home's name
   * @return The formatted message
   */
  static Component teleportingHome(String homeName) {
    return format("Teleporting to {0}.", NamedTextColor.GRAY, homeName);
  }

  /**
   * Lists the homes in the given home map. This only lists the entries, this does not include any
   * header
   *
   * @param homes The home map to list
   * @param cmd   The command prefix to use for the entry's {@link net.kyori.adventure.text.event.ClickEvent}s
   * @return The formatted message
   */
  static Component listHomes(UserHomes homes, String cmd) {
    return TextJoiner.onComma()
        .add(
            homes.getHomes().entrySet().stream()
                .map(entry -> {
                  return text("[" + entry.getKey() + "]", NamedTextColor.GOLD)
                      .hoverEvent(Text.prettyLocation(entry.getValue(), false))
                      .clickEvent(runCommand(cmd + entry.getKey()));
                })
        )
        .asComponent();
  }

  /**
   * Creates a home list header with the given user's display name
   *
   * @param user The homes' owner
   * @return The formatted message
   */
  static Component homeListHeader(User user) {
    return format("{0, user}'s homes", NamedTextColor.GOLD, user);
  }

  /**
   * Creates a message saying the given user's home with the given name was deleted
   *
   * @param user The user whose home was deleted
   * @param home The name of the deleted home
   * @return The formatted message
   */
  static Component deletedHomeOther(User user, String home) {
    return format("Deleted &6{0, user}&r's home: '&6{1}&r'",
        NamedTextColor.YELLOW,
        user, home
    );
  }

  /**
   * Creates a message saying that a home by the given name was deleted
   *
   * @param home The name of the deleted home
   * @return The formatted message
   */
  static Component deletedHomeSelf(String home) {
    return format("Deleted home '&6{0}&r'",
        NamedTextColor.YELLOW, home
    );
  }


  static Component withdrew(int items, int earned) {
    String format = items == 1 ? WITHDRAW_FORMAT_SINGLE : WITHDRAW_FORMAT_MULTIPLE;

    return format(format,
        NamedTextColor.GRAY,
        UnitFormat.coins(items), earned
    );
  }

  static Component deposit(int coins, int earned) {
    return format("You deposited &e{0}&r and received &6{1, rhines}&r.",
        NamedTextColor.GRAY,
        UnitFormat.coins(coins), earned
    );
  }

  /**
   * Lists all blocked users
   *
   * @param users The users to list
   * @return The formatted component
   */
  static Component listBlocked(Collection<UUID> users, Audience viewer) {
    return joinIds(users, IGNORE_LIST_HEADER, viewer);
  }

  static Component joinIds(Collection<UUID> uuids, Component header) {
    return joinIds(uuids, header, null);
  }

  static Component joinIds(Collection<UUID> uuids, Component header, Audience viewer) {
    return TextJoiner.onComma()
        .setColor(NamedTextColor.GOLD)
        .setPrefix(header)
        .add(uuids.stream().map(uuid -> {
          var user = Users.get(uuid);
          return user.displayName(viewer).color(NamedTextColor.YELLOW);
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

  /**
   * Creates a header for the <code>/list</code> command
   *
   * @param userCount The amount of online users
   * @return The formatted message
   */
  static Component listHeader(int userCount) {
    return format("There are &6{0, number}&r out of &6{1, number}&r players online.",
        NamedTextColor.YELLOW,
        userCount, Bukkit.getMaxPlayers()
    );
  }

  /**
   * Lists all players in the given collection. This will also prepend 'Players: ' onto the front of
   * that list.
   *
   * @param users The users to list, vanished or afk players will not be ignored
   * @return The formatted component
   */
  static Component listPlayers(Collection<User> users, Audience viewer) {
    return TextJoiner.onComma()
        .setColor(NamedTextColor.YELLOW)
        .setPrefix(text("Players: "))

        // Add users
        .add(users.stream()
            .map(user -> {
              var text = text()
                  .color(NamedTextColor.WHITE)
                  .append(user.displayName(viewer));

              if (user.get(Properties.VANISHED)) {
                text.append(VANISHED_LIST_SUFFIX);
              }

              return text.build();
            })
        )

        // Return result
        .asComponent();
  }


  /**
   * Message stating the viewer's nickname was cleared
   */
  Component NICK_CLEARED = text("Cleared nickname", NamedTextColor.GRAY);

  /**
   * Creates a message stating the viewer set their nickname to the given nick
   *
   * @param nick The set nickname
   * @return The formatted message
   */
  static Component nickSetSelf(String nick) {
    return format("Nickname set to '&f{0}&r'", NamedTextColor.GRAY, nick);
  }

  /**
   * Creates a message stating the given user's nickname was set to the given value
   *
   * @param user The user whose nickname was changed
   * @param nick The value their nick was set to
   * @return The formatted message
   */
  static Component nickSetOther(User user, String nick) {
    return format("Set &e{0}&r's nickname to '&f{1}&r'",
        NamedTextColor.GRAY,
        user.displayName(), nick
    );
  }

  /**
   * Creates a message stating the user's given nickname was cleared
   *
   * @param user The user whose nickname was cleared
   * @return The formatted message
   */
  static Component nickClearOther(User user) {
    return format("Cleared &e{0, user}&r's nickname.",
        NamedTextColor.GRAY, user
    );
  }

  /**
   * Creates a message saying the item held by the given user was repaired
   *
   * @param user The user holding the item
   * @return The formatted message
   */
  static Component repairedItem(User user) {
    return format("Repaired item held by &6{0, user}&r.", NamedTextColor.YELLOW, user);
  }

  /**
   * Creates a message saying the given user is being healed
   *
   * @param target The user being healed
   * @return The formatted message
   */
  static Component healing(User target) {
    return format("Healing &6{0, user}&r!", NamedTextColor.YELLOW, target);
  }

  /**
   * Creates a message saying the given user was fed
   *
   * @param target The user that was fed
   * @return The formatted message
   */
  static Component feeding(User target) {
    return format("Satiated the appetite of &6{0, user}&r!", NamedTextColor.YELLOW, target);
  }

  static Component addedEnchant(Enchantment enchantment, int level) {
    return format("Added enchantment '&e{0}&r' to held item.",
        NamedTextColor.GRAY,
        enchantment.displayName(level)
    );
  }

  static Component removedEnchant(Enchantment enchantment) {
    return format("Removed enchantment '&e{0}&r' from held item.",
        NamedTextColor.GRAY,
        enchantment.displayName(1)
    );
  }

  static Component removedLoreIndex(int index) {
    return format("Removed lore at line &e{0, number}&r.",
        NamedTextColor.GRAY,
        index
    );
  }

  static Component removedLoreRange(Range<Integer> range) {
    return format("Removed lore from &e{0, number}&r to &e{1, number}&r.",
        NamedTextColor.GRAY,
        range.getMinimum(),
        range.getMaximum()
    );
  }

  static Component addedLore(Component text) {
    return format("Added '&f{0}&r' to lore",
        NamedTextColor.GRAY,
        text
    );
  }

  static Component setItemName(Component name) {
    return format("Set item name to '&f{0}&r'",
        NamedTextColor.GRAY,
        name
    );
  }

  static Component addedAttributeModifier(Attribute attr, AttributeModifier mod) {
    var hover = text(
        mod.toString()
            .replaceAll(", ", "\n")
            .replaceAll("AttributeModier\\{", "Modifier data:{\n")
            .replaceAll("}", "\n}")
    );

    return format("Added modifier for attribute '&e{0}&r'",
        NamedTextColor.GRAY,
        Component.text(attr.key().asString())
            .hoverEvent(hover)
    );
  }


  static ViewerAwareMessage paidMultiple(int paid, int amount) {
    return vformat("Paid &6{0, number}&r players &6{1, rhines}&r, total: &6{2, rhines}&r.",
        NamedTextColor.YELLOW,
        paid, amount,
        paid * amount
    );
  }

  static ViewerAwareMessage payTarget(User sender, int amount, @Nullable Component message) {
    if (isEmpty(message)) {
      return vformat("You received &6{1, rhines}&r from &e{0, user}&r.",
          NamedTextColor.GRAY,
          sender, amount
      );
    }

    return vformat("You received &6{1, rhines}&r from &e{0, user}&r: &f{2}",
        NamedTextColor.GRAY,
        sender, amount, message
    );
  }

  static ViewerAwareMessage paySender(User target, int amount, @Nullable ViewerAwareMessage message) {
    if (isEmpty(message)) {
      return vformat("Paid &6{0, rhines}&r to &e{1, user}&r.",
          NamedTextColor.GRAY,
          amount, target
      );
    }

    return vformat("Paid &6{0, rhines}&r to &e{1, user}&r: &f{2}",
        NamedTextColor.GRAY,
        amount, target, message
    );
  }
}
