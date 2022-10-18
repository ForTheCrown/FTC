package net.forthecrown.useables;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.WorldBounds3i;
import net.minecraft.nbt.CompoundTag;

/**
 * A trigger which holds actions and triggers that
 * is activated when a user enters its area of effect
 */
public class UsableTrigger extends AbstractUsable {
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
    private final WorldBounds3i area;

    /** The trigger's type, specifies when the trigger is activated */
    @Getter @Setter
    private TriggerType type = TriggerType.ENTER;

    public UsableTrigger(String name, WorldBounds3i area) {
        this.name = name;
        this.area = area;

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
        writer.field("Area", area);
        writer.field("Type", type.name().toLowerCase());
        writer.newLine();

        super.adminInfo(writer);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put(TAG_AREA, area.save());
        tag.put(TAG_TYPE, TagUtil.writeEnum(type));

        super.save(tag);
    }
}