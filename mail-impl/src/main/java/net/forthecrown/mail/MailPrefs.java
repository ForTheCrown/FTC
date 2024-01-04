package net.forthecrown.mail;

import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;

public final class MailPrefs {
  private MailPrefs() {}

  public static final UserProperty<Boolean> MAIL_TO_DISCORD
      = Properties.booleanProperty("mailToDiscord", false);

  static void init(SettingsBook<User> book) {
    var setting = Setting.create(MAIL_TO_DISCORD)
        .setDisplayName("Discord Mail")
        .setDescription("Toggles received mail being forwarded to your discord DMs with the FTC bot")
        .setToggle("N{1} forwarding in-game mail to discord")
        .setToggleDescription("{Enable} in-game mail forwarding to discord")

        .createCommand(
            "maildiscord",
            MailPermissions.MAIL,
            MailPermissions.MAIL_ADMIN,
            "togglediscordmail",
            "discordmail"
        );

    book.getSettings().add(setting.toBookSettng());
  }
}
