package net.forthecrown.marriages;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import net.forthecrown.Permissions;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.mail.Mail;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.format.FormatBuilder;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.forthecrown.user.Users;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Marriages {
  private Marriages() {}

  public static final UserProperty<UUID> SPOUSE
      = Properties.uuidProperty().key("spouse").build();

  public static final UserProperty<Boolean> MCHAT_TOGGLED
      = Properties.booleanProperty("marriageChatToggle", false);

  public static final UserProperty<Boolean> ACCEPTING_PROPOSALS
      = Properties.booleanProperty("acceptingProposals", true);

  static final Random MESSAGE_RANDOM = new Random();

  static void defineSettings(SettingsBook<User> settingsBook) {
    Setting mchatSetting = Setting.create(MCHAT_TOGGLED)
        .setDisplayName("MChat")
        .setDescription("Toggles all your chat messages going to a private chat with your spouse")
        .setToggle("Your messages will n{1} be sent to your spouse.")
        .setToggleDescription("{Enable} all chat messages going to a private marriage chat")

        .setValidator((user, newState) -> {
          var spouse = getSpouse(user);

          if (spouse == null) {
            throw MExceptions.NOT_MARRIED;
          }

          if (newState && !spouse.isOnline()) {
            throw Exceptions.notOnline(spouse);
          }
        })

        .createCommand(
            "marriagechattoggle",
            MPermissions.MARRY,
            Permissions.ADMIN,
            "mct", "mchattoggle", "mctoggle"
        );

    Setting proposalToggle = Setting.create(ACCEPTING_PROPOSALS)
        .setDisplayName("Marrying")
        .setDescription("Toggles being able to send and receive marriage proposals")
        .setToggle("N{1} accepting marriage proposals.")
        .setToggleDescription("{Enable} sending and receiving marriage proposals")

        .createCommand(
            "marrytoggle",
            MPermissions.MARRY,
            Permissions.ADMIN,
            "togglemarry"
        );

    settingsBook.getSettings().add(mchatSetting.toBookSettng());
    settingsBook.getSettings().add(proposalToggle.toBookSettng());
  }

  public static User getSpouse(User user) {
    var spouseId = user.get(SPOUSE);

    if (spouseId.equals(Identity.nil().uuid())) {
      return null;
    }

    return Users.get(spouseId);
  }

  public static boolean areMarried(User user1, User user2) {
    return Objects.equals(getSpouse(user1), user2);
  }

  public static void setMarried(User user, User spouse) {
    spouse.set(SPOUSE, user.getUniqueId());
    user.set(SPOUSE, spouse.getUniqueId());
  }

  public static void setDivorced(User user) {
    var spouse = getSpouse(user);

    if (spouse == null) {
      return;
    }

    spouse.set(SPOUSE, null);
    user.set(SPOUSE, null);
  }

  public static void divorce(User user) {
    var spouse = getSpouse(user);
    Preconditions.checkState(spouse != null, "User is not married, cannot divorce");

    user.set(SPOUSE, null);
    spouse.set(SPOUSE, null);

    // Ensure neither is in marriage chat
    user.set(MCHAT_TOGGLED, false);
    spouse.set(MCHAT_TOGGLED, false);

    user.sendMessage(MMessages.senderDivorced(spouse));
    Mail.sendOrMail(spouse, MMessages.targetDivorced(user));
  }

  public static void testCanMarry(User user, User target) throws CommandSyntaxException {
    // Not self lol
    if (target.equals(user)) {
      throw MExceptions.MARRY_SELF;
    }

    var targetSpouse = getSpouse(target);
    var userSpouse = getSpouse(user);

    // Both are unmarried
    if (userSpouse != null) {
      throw MExceptions.senderAlreadyMarried(userSpouse);
    }

    if (targetSpouse != null) {
      throw MExceptions.targetAlreadyMarried(target, targetSpouse);
    }

    // Both accepting proposals
    if (!user.get(ACCEPTING_PROPOSALS)) {
      throw MExceptions.MARRY_DISABLED_SENDER;
    }

    if (!target.get(ACCEPTING_PROPOSALS)) {
      throw MExceptions.marriageDisabledTarget(target);
    }
  }

  public static void marry(User user, User target) {
    setMarried(user, target);

    target.sendMessage(MMessages.nowMarried(user).create(target));
    user.sendMessage(MMessages.nowMarried(target).create(user));

    ChannelledMessage.announce(
        FormatBuilder.builder()
            .setFormat("&e{0, user}&r is now married to &e{1, user}&r{2}")
            .setArguments(
                user, target,

                MESSAGE_RANDOM.nextInt(0, 1000) != 1
                    ? text("!")
                    : text("... I give it a week", NamedTextColor.GRAY)
            )
            .asViewerAware()
    );
  }

  public static void mchat(User user, PlayerMessage message) {
    var spouse = getSpouse(user);

    assert spouse != null : "No spouse";

    var chn = ChannelledMessage.create(message);
    chn.setChannelName("marriage_chat");
    chn.setSource(user);
    chn.addTarget(spouse);

    chn.setRenderer((viewer, baseMessage) -> {
      Component displayName = user.displayName(viewer);

      return text()
          .append(MMessages.MARRIAGE_PREFIX)
          .append(displayName.color(NamedTextColor.GOLD))
          .append(MMessages.MARRIAGE_POINTER)
          .append(baseMessage)
          .build();
    });

    chn.send();
  }
}
