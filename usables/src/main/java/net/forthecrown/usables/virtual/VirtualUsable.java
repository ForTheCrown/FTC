package net.forthecrown.usables.virtual;

import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.usables.objects.Usable;
import net.kyori.adventure.text.Component;

public class VirtualUsable extends Usable {

  @Getter
  private String name;

  @Setter @Getter
  private boolean cancelVanilla;

  @Getter
  VirtualUsableManager manager;

  public VirtualUsable(String name) {
    Objects.requireNonNull(name, "Null name");
    this.name = name;
  }

  public void setName(String name) {
    Objects.requireNonNull(name, "Null name");

    if (manager != null && manager.containsUsable(name)) {
      throw new IllegalStateException("Name '" + name + "' is already in use");
    }

    var manager = this.manager;

    if (manager != null) {
      manager.internalRemove(name);
    }

    this.name = name;

    if (manager != null) {
      manager.internalAdd(this);
    }
  }

  @Override
  public void fillContext(Map<String, Object> context) {
    context.put("cancelVanilla", cancelVanilla);
  }

  @Override
  public String getCommandPrefix() {
    return "/usables virtual " + getName();
  }

  @Override
  public Component name() {
    return Component.text(getName());
  }
}
