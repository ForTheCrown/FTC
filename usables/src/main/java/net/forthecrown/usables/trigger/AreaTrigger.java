package net.forthecrown.usables.trigger;

import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.usables.objects.Usable;
import net.forthecrown.usables.virtual.RegionAction;
import net.forthecrown.usables.virtual.TriggerMap;
import net.forthecrown.utils.io.TagOps;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;

@Getter
public class AreaTrigger extends Usable {

  private String name;

  private WorldBounds3i area;

  @Setter
  private Type type;

  TriggerManager manager;

  final TriggerMap<RegionAction> externalTriggers;

  public AreaTrigger() {
    externalTriggers = new TriggerMap<>(RegionAction.CODEC);
  }

  @Override
  public Component name() {
    return Component.text(getName());
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);

    externalTriggers.clear();
    if (tag.contains("external_triggers")) {
      externalTriggers.load(new Dynamic<>(TagOps.OPS, tag.get("external_triggers")))
          .mapError(s -> "Error loading '" + name + "' external triggers: " + s)
          .resultOrPartial(LOGGER::error);
    }

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

    if (!externalTriggers.isEmpty()) {
      externalTriggers.save(TagOps.OPS)
          .mapError(s -> "Failed to save external triggers inside '" + name + "': " + s)
          .resultOrPartial(LOGGER::error)
          .ifPresent(binaryTag -> tag.put("external_triggers", binaryTag));
    }
  }

  @Override
  public void fillContext(Map<String, Object> context) {
    super.fillContext(context);
    context.put("area", area);
    context.put("triggerName", name);
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
