package net.forthecrown.titles;

import net.forthecrown.Permissions;
import net.forthecrown.command.settings.Setting;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;

public class TitleSettings {

  public static final UserProperty<Boolean> SEE_RANKS = Properties.booleanProperty()
      .defaultValue(false)
      .key("rankedNameTags")
      .build();

  static void add(SettingsBook<User> settingsBook) {
    var setting = Setting.create(SEE_RANKS)
        .setDescription("Toggles seeing ranks in chat")
        .setDisplayName("Ranks in chat")
        .setToggleMessage("N{1} showing ranks in chat")
        .createCommand("rankchat", Permissions.DEFAULT, Permissions.ADMIN, "chatranks");

    settingsBook.getSettings().add(setting.toBookSettng());
  }
}
