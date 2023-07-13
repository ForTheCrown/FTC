package net.forthecrown.user;

import net.forthecrown.text.Text;
import net.forthecrown.user.name.UserNameFactory;
import net.forthecrown.user.name.NameElement;
import net.kyori.adventure.text.Component;

public final class NameElements {
  private NameElements() {}

  public static final NameElement EMPTY = (user, context) -> null;

  public static final NameElement PREFIX_PROPERTY
      = (user, context) -> user.get(Properties.PREFIX);

  public static final NameElement SUFFIX_PROPERTY
      = (user, context) -> user.get(Properties.SUFFIX);

  public static void registerAll(UserNameFactory factory) {
    factory.addPrefix(PREFIX_PROPERTY);
    factory.addSuffix(SUFFIX_PROPERTY);
  }

  public static NameElement combine(NameElement n1, NameElement n2) {
    return (user, context) -> {
      Component first = n1.createDisplay(user, context);

      if (Text.isEmpty(first)) {
        return n2.createDisplay(user, context);
      }

      return first;
    };
  }
}