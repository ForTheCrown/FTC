package net.forthecrown.core;

import java.util.List;
import net.forthecrown.Permissions;
import net.forthecrown.command.settings.BookSetting;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.core.commands.tpa.TpPermissions;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;

public class PrefsBook {

  static void init(SettingsBook<User> settings) {
    Setting flying = Setting.create(Properties.FLYING)
        .setDisplayName("Flying")
        .setDescription("Toggles being able to fly")
        .setToggle("{0} flying")
        .setToggleDescription("{Enable} flying")

        .createCommand(
            "fly",
            CorePermissions.FLY,
            CorePermissions.FLY_OTHERS,
            "flight"
        );

    Setting god = Setting.create(Properties.GODMODE)
        .setDisplayName("Godmode")
        .setDescription("Toggles godmode")
        .setToggle("{0} God mode")
        .setToggleDescription("{Enable} God mode")

        .createCommand(
            "god",
            CorePermissions.GOD,
            CorePermissions.GOD_OTHERS,
            "godmode"
        );

    Setting tpa = Setting.create(Properties.TPA)
        .setDisplayName("TPA")
        .setDescription("Toggles being able to send and receive TPA requests")
        .setToggle("{0} TPA requests")
        .setToggleDescription("{Enable} TPA requests")

        .createCommand(
            "tpatoggle",
            TpPermissions.TPA,
            Permissions.ADMIN,
            "toggletpa"
        );

    Setting profilePrivate = Setting.create(Properties.PROFILE_PRIVATE)
        .setDisplayName("PrivateProfile")
        .setDescription("Toggles other players being able to see your /profile")
        .setToggle("Others can n{2} see your /profile")
        .setEnableDescription("Set your profile to private")
        .setDisableDescription("Set your profile to public")

        .createCommand(
            "profileprivate",
            Permissions.PROFILE,
            Permissions.ADMIN,
            "profiletoggle", "toggleprofile", "profilepublic"
        );

    List<BookSetting<User>> list = settings.getSettings();
    list.add(flying.toBookSettng());
    list.add(god.toBookSettng());
    list.add(tpa.toBookSettng());
    list.add(profilePrivate.toBookSettng());
  }
}
