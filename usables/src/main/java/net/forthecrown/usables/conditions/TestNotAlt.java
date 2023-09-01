package net.forthecrown.usables.conditions;

import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.SimpleType;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TestNotAlt implements Condition {

  public static final UsageType<TestNotAlt> TYPE = new SimpleType<>(TestNotAlt::new);

  @Override
  public boolean test(Interaction interaction) {
    UserService service = Users.getService();
    return !service.isAltAccount(interaction.playerId());
  }

  @Override
  public Component failMessage(Interaction interaction) {
    return Component.text("Alt accounts may not use this", NamedTextColor.GRAY);
  }

  @Override
  public UsageType<? extends UsableComponent> getType() {
    return TYPE;
  }
}
