package net.forthecrown.inventory;

import net.forthecrown.user.Properties;
import net.forthecrown.user.UserProperty;

public final class ItemUserProperties {
  private ItemUserProperties() {}

  public static final UserProperty<Boolean> SKIP_ABILITY_ANIM
      = Properties.booleanProperty("skipAbilityAnimation", false);

  static void init() {

  }
}
