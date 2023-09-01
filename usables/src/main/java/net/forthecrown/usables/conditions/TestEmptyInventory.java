package net.forthecrown.usables.conditions;

import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.SimpleType;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TestEmptyInventory implements Condition {

  public static final UsageType<TestEmptyInventory> TYPE
      = new SimpleType<>(TestEmptyInventory::new);

  @Override
  public boolean test(Interaction interaction) {
    return interaction.player().getInventory().isEmpty();
  }

  @Override
  public Component failMessage(Interaction interaction) {
    return Component.text("Your inventory must be empty", NamedTextColor.GRAY);
  }

  @Override
  public UsageType<? extends UsableComponent> getType() {
    return TYPE;
  }
}
