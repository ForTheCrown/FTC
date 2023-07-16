package net.forthecrown.antigrief;

import java.util.List;
import net.forthecrown.command.settings.BookSetting;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;

public final class EavesDropper {
  private EavesDropper() {}

  /**
   * Determines if a user sees a muted user's chat messages with {@link EavesDropper}
   * <p>
   * Only affects users with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_MUTED
      = Properties.booleanProperty("eavesDrop_muted", false);

  /**
   * Determines if a user sees other people's direct messages through Eavesdropper
   * <p>
   * Only affects users with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_DM
      = Properties.booleanProperty("eavesDrop_dm", false);

  /**
   * Determines if a user sees what people write on signs with EavesDropper
   * <p>
   * Only affects users with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_SIGN
      = Properties.booleanProperty("eavesDrop_signs", false);

  /**
   * Determines if a user sees what other married users send to each other through marriage chat
   * with EavesDropper
   * <p>
   * Only affects users with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_MCHAT
      = Properties.booleanProperty("eavesDrop_mChat", false);

  /**
   * Determines if a user sees eaves dropper messages when other users mine into veins
   * <p>
   * Only affects uses with {@link GriefPermissions#EAVESDROP}
   */
  public static final UserProperty<Boolean> EAVES_DROP_MINING
      = Properties.booleanProperty("eavesDrop_mining", false);

  /**
   * Determines if a user sees eaves dropper messages when guild members use their respective
   * guild's guild chat.
   */
  public static final UserProperty<Boolean> EAVES_DROP_GUILD_CHAT
      = Properties.booleanProperty("eavesDrop_guildChat", false);

  static void createSettings(SettingsBook<User> settingsBook) {
    Setting muted = Setting.create(EAVES_DROP_MUTED)
        .setDisplayName("Spy on muted")
        .setToggleMessage("{Start} spying on muted players' messages")
        .setDescription("Toggles spying on people's muted messages");

    muted.createCommand(
        "eavesdrop_muted",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seemuted", "see_muted"
    );

    Setting dms = Setting.create(EAVES_DROP_DM)
        .setDisplayName("Spy on DMs")
        .setToggleMessage("{Start} spying on players' DMs")
        .setDescription("Toggles spying on people's direct messages");

    dms.createCommand(
        "eavesdrop_dm",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seedms", "see_dms"
    );

    Setting signs = Setting.create(EAVES_DROP_SIGN)
        .setDisableDescription("Spy on signs")
        .setDescription("Toggles spying on what people write on signs");

    signs.createCommand(
        "eavesdrop_signs",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seesigns", "see_signs"
    );

    Setting mchat = Setting.create(EAVES_DROP_MCHAT)
        .setDisplayName("Spy on mchat")
        .setDescription("Toggles spying on people's marriage DMs")
        .setToggleDescription("{Start} spying on people's marriage DMs");

    mchat.createCommand(
        "eavesdrop_mchat",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seemchat", "seemarriage", "see_mchat", "see_marriage"
    );

    Setting gchat = Setting.create(EAVES_DROP_GUILD_CHAT)
        .setDisplayName("Spy on guilds")
        .setDescription("Toggles spying on guild chats")
        .setToggleDescription("{Start} spying on guild chats");

    gchat.createCommand(
        "eavesdrop_gchat",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN,
        "seegchat", "see_gchat"
    );

    Setting veins = Setting.create(EAVES_DROP_MINING)
        .setDisplayName("Spy on miners")
        .setEnableDescription("Get notified when people mine ores")
        .setDisableDescription("Don't get notified when people mine ores")
        .setDescription("Toggles being notified when players mine ores");

    veins.createCommand(
        "eavesdrop_veins",
        GriefPermissions.EAVESDROP,
        GriefPermissions.EAVESDROP_ADMIN
    );

    List<BookSetting<User>> settings = settingsBook.getSettings();
    settings.add(muted.toBookSettng());
    settings.add(dms.toBookSettng());
    settings.add(signs.toBookSettng());
    settings.add(mchat.toBookSettng());
    settings.add(gchat.toBookSettng());
    settings.add(veins.toBookSettng());
  }


}
