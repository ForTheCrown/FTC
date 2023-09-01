package net.forthecrown.usables.objects;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Condition.TransientCondition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.Usables;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Getter
public abstract class CommandUsable extends Usable {

  private final String name;

  private TransientCondition additional;

  public CommandUsable(String name) {
    super();

    Objects.requireNonNull(name, "Null name");
    this.name = name;
  }

  @Override
  public void fillContext(Map<String, Object> context) {
    super.fillContext(context);
    context.put("name", name);
  }

  @Override
  public Component name() {
    return Component.text(getName());
  }

  @Override
  public boolean interact(Interaction interaction) {
    if (!runConditions(interaction)) {
      return false;
    }

    Usables.runActions(getActions(), interaction);

    onInteract(interaction.player(), interaction.getBoolean("adminInteraction").orElse(false));
    return true;
  }

  protected abstract void onInteract(Player player, boolean adminInteraction);

  @Override
  public Iterable<Condition> getEffectiveConditions() {
    var additional = getAdditional();

    if (additional == null) {
      return getConditions();
    }

    return Iterables.concat(getConditions(), List.of(additional));
  }

  private TransientCondition getAdditional() {
    return additional == null ? (additional = additionalCondition()) : additional;
  }

  protected TransientCondition additionalCondition() {
    return null;
  }
}
