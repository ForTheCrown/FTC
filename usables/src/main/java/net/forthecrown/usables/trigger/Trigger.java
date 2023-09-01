package net.forthecrown.usables.trigger;

import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.usables.objects.Usable;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;

@Getter
public class Trigger extends Usable {

  private String name;

  private WorldBounds3i area;

  @Setter
  private Type type;

  TriggerManager manager;

  public Trigger() {

  }

  @Override
  public Component name() {
    return Component.text(getName());
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);

    this.type = TagUtil.readEnum(Type.class, tag.get("type"));
    this.area = WorldBounds3i.of(tag.getCompound("area"));
  }

  @Override
  public String getCommandPrefix() {
    return "/triggers " + getName() + " ";
  }

  @Override
  public void save(CompoundTag tag) {
    super.save(tag);
    tag.put("type", TagUtil.writeEnum(type));
    tag.put("area", area.save());
  }

  @Override
  public void fillContext(Map<String, Object> context) {
    context.put("_area", area);
  }

  public void setArea(WorldBounds3i area) {
    Objects.requireNonNull(area);

    TriggerManager manager = this.manager;

    if (manager != null) {
      manager.remove(this);
    }

    this.area = area;

    if (manager != null) {
      manager.add(this);
    }
  }

  public void setName(String name) {
    Objects.requireNonNull(name);

    TriggerManager manager = this.manager;

    if (manager != null) {
      manager.remove(this);
    }

    this.name = name;

    if (manager != null) {
      manager.add(this);
    }
  }

  public enum Type {
    ENTER, EXIT, EITHER, MOVE
  }
}
