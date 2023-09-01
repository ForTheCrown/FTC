package net.forthecrown.usables;

import net.kyori.adventure.text.Component;

public interface Condition extends UsableComponent {

  boolean test(Interaction interaction);

  default Component failMessage(Interaction interaction) {
    return null;
  }

  default void afterTests(Interaction interaction) {

  }

  interface TransientCondition extends Condition {

    @Override
    default UsageType<? extends UsableComponent> getType() {
      return null;
    }
  }
}
