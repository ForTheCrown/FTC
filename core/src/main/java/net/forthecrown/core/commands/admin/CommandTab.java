package net.forthecrown.core.commands.admin;

import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandFile;
import net.forthecrown.text.Text;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@CommandFile("commands/tab.gcn")
public class CommandTab {

  void setSuffix(CommandSource source, @Argument("user") User user, @Argument("text") Component text) {
    set(source, user, "suffix", Properties.SUFFIX, text);
  }

  void unsetSuffix(CommandSource source, @Argument("user") User user) {
    unset(source, user, "suffix", Properties.SUFFIX);
  }

  void setPrefix(CommandSource source, @Argument("user") User user, @Argument("text") Component text) {
    set(source, user, "prefix", Properties.PREFIX, text);
  }

  void unsetPrefix(CommandSource source, @Argument("user") User user) {
    unset(source, user, "prefix", Properties.PREFIX);
  }

  void setName(CommandSource source, @Argument("user") User user, @Argument("text") Component text) {
    set(source, user, "display name", Properties.TAB_NAME, text);
  }

  void unsetName(CommandSource source, @Argument("user") User user) {
    unset(source, user, "display name", Properties.TAB_NAME);
  }

  void unset(
      CommandSource source,
      User user,
      String name,
      UserProperty<Component> property
  ) {
    Component before = user.displayName(source);
    user.set(property, null);

    source.sendMessage(
        Text.format("Removed &e{0}&r's tab {1}.", NamedTextColor.GRAY, before, name)
    );
  }

  void set(
      CommandSource source,
      User user,
      String name,
      UserProperty<Component> property,
      Component text
  ) {
    if (Text.isDashClear(text)) {
      unset(source, user, name, property);
      return;
    }

    Component before = user.displayName(source);
    user.set(property, text);

    source.sendMessage(
        Text.format("Set &e{0}&r's tab {1} to &f{2}&r.",
            NamedTextColor.GRAY,
            before, name, text
        )
    );
  }
}
