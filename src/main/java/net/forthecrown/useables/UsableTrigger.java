package net.forthecrown.useables;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.BoundsHolder;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.text.writer.TextWriter;

/**
 * A trigger which holds actions and triggers that is activated when a user enters its area of
 * effect
 */
public class UsableTrigger extends Usable implements BoundsHolder {

  private static final String
      TAG_AREA = "area",
      TAG_TYPE = "type";

  /**
   * The ID of this trigger
   */
  @Getter
  private String name;

  /**
   * The area of effect of this trigger
   */
  @Getter
  private WorldBounds3i bounds;

  /**
   * The trigger's type, specifies when the trigger is activated
   */
  @Getter
  @Setter
  private TriggerType type = TriggerType.ENTER;

  TriggerManager manager;

  public UsableTrigger(String name, WorldBounds3i bounds) {
    this.name = name;
    this.bounds = bounds;

    setSilent(true);
  }

  public UsableTrigger(String name, CompoundTag tag) {
    this(name, WorldBounds3i.of(tag.getCompound(TAG_AREA)));
    load(tag);

    if (tag.contains(TAG_TYPE)) {
      this.type = TagUtil.readEnum(TriggerType.class, tag.get(TAG_TYPE));
    }
  }

  public void setName(String name) {
    Objects.requireNonNull(name);

    var manager = this.manager;
    if (manager != null) {
      manager.remove(this);
    }

     this.name = name;

    if (manager != null) {
      manager.add(this);
    }
  }

  public void setBounds(WorldBounds3i bounds) {
    Objects.requireNonNull(bounds);

    var manager = this.manager;
    if (manager != null) {
      manager.remove(this);
    }

    this.bounds = bounds;

    if (manager != null) {
      manager.add(this);
    }
  }

  @Override
  public void adminInfo(TextWriter writer) {
    writer.field("Name", name);
    writer.field("Area", bounds);
    writer.field("Type", type.name().toLowerCase());
    writer.newLine();

    super.adminInfo(writer);
  }

  @Override
  public void save(CompoundTag tag) {
    tag.put(TAG_AREA, bounds.save());
    tag.put(TAG_TYPE, TagUtil.writeEnum(type));

    super.save(tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof UsableTrigger trigger)) {
      return false;
    }

    return Objects.equals(type, trigger.type)
        && Objects.equals(name, trigger.name);
  }
}