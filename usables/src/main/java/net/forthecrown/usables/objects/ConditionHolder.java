package net.forthecrown.usables.objects;

import java.util.function.Predicate;
import net.forthecrown.usables.ComponentList;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.Usables;

public interface ConditionHolder extends Predicate<Interaction>, UsableObject {

  ComponentList<Condition> getConditions();

  default Iterable<Condition> getEffectiveConditions() {
    return getConditions();
  }

  boolean isSilent();

  void setSilent(boolean silent);

  @Override
  default boolean test(Interaction interaction) {
    return Usables.test(getEffectiveConditions(), interaction);
  }

  default boolean runConditions(Interaction interaction) {
    return Usables.runConditions(getEffectiveConditions(), interaction);
  }
}
