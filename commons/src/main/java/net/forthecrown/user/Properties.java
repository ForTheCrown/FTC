package net.forthecrown.user;

import java.util.UUID;
import net.kyori.adventure.text.Component;

public final class Properties {
  private Properties() {}

  public static final UserProperty<Boolean> VANISHED = booleanProperty()
      .key("vanished")
      .defaultValue(false)
      .callback((user, value, oldValue) -> user.updateVanished())
      .build();

  public static final UserProperty<Boolean> FLYING = booleanProperty()
      .key("flying")
      .defaultValue(false)
      .callback((user, value, oldValue) -> user.updateFlying())
      .build();

  public static final UserProperty<Boolean> GODMODE = booleanProperty()
      .key("godMode")
      .defaultValue(false)
      .callback((user, value, oldValue) -> user.updateGodMode())
      .build();

  public static final UserProperty<Boolean> TPA = booleanProperty()
      .defaultValue(true)
      .key("tpa")
      .build();

  public static UserProperty<Component> PREFIX = textProperty()
      .key("prefix")
      .callback((user, value, oldValue) -> user.updateTabName())
      .defaultValue(Component.empty())
      .build();

  public static UserProperty<Component> SUFFIX = textProperty()
      .key("suffix")
      .callback((user, value, oldValue) -> user.updateTabName())
      .defaultValue(Component.empty())
      .build();

  public static UserProperty<Component> TAB_NAME = textProperty()
      .key("tabName")
      .callback((user, value, oldValue) -> user.updateTabName())
      .defaultValue(Component.empty())
      .build();

  public static UserProperty.Builder<Boolean> booleanProperty() {
    return Users.getService().createBooleanProperty();
  }

  public static UserProperty.Builder<Component> textProperty() {
    return Users.getService().createTextProperty();
  }

  public static <E extends Enum<E>> UserProperty.Builder<E> enumProperty(Class<E> type) {
    return Users.getService().createEnumProperty(type);
  }

  public static UserProperty.Builder<UUID> uuidProperty() {
    return Users.getService().createUuidProperty();
  }
}