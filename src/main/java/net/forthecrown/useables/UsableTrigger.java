package net.forthecrown.useables;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.utils.BoundsHolder;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.WorldBounds3i;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

/**
 * A trigger which holds actions and triggers that
 * is activated when a user enters its area of effect
 */
public class UsableTrigger extends AbstractUsable implements BoundsHolder {
    private static final String
            TAG_AREA = "area",
            TAG_TYPE = "type";

    /**
     * The ID of this trigger
     */
    @Getter
    private final String name;

    /**
     * The area of effect of this trigger
     */
    @Getter
    private final WorldBounds3i bounds;

    /** The trigger's type, specifies when the trigger is activated */
    @Getter @Setter
    private TriggerType type = TriggerType.ENTER;

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