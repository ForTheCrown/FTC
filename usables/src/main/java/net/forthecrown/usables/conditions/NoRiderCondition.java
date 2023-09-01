package net.forthecrown.usables.conditions;

import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.SimpleType;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NoRiderCondition implements Condition {

  public static final UsageType<NoRiderCondition> TYPE = new SimpleType<>(NoRiderCondition::new);

  @Override
  public boolean test(Interaction interaction) {
    return interaction.player().getPassengers().isEmpty();
  }

  @Override
  public Component failMessage(Interaction interaction) {
    return Component.text("Cannot have anyone riding you", NamedTextColor.GRAY);
  }

  @Override
  public UsageType<? extends UsableComponent> getType() {
    return TYPE;
  }
}
