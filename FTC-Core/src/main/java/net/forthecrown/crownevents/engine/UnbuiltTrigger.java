package net.forthecrown.crownevents.engine;

import net.forthecrown.crownevents.entries.EventEntry;
import org.bukkit.util.BoundingBox;

public record UnbuiltTrigger<E extends EventEntry>(LocOffset minOffset,
                                                   LocOffset maxOffset,
                                                   TriggerAction<E> action
) {
    public static <E extends EventEntry> UnbuiltTrigger<E> create(BoundingBox offsetBox, TriggerAction<E> action) {
        LocOffset min = LocOffset.of(offsetBox.getMin());
        LocOffset max = LocOffset.of(offsetBox.getMax());

        return new UnbuiltTrigger<>(min, max, action);
    }

    public ArenaTrigger<E> build(ArenaBuildContext context) {
        BoundingBox box = BoundingBox.of(
                minOffset.apply(context.getBuildLocation()),
                maxOffset.apply(context.getBuildLocation())
        );

        return new ArenaTrigger<>(box, action);
    }
}
