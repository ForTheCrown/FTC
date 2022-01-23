package net.forthecrown.crownevents.engine;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.crownevents.entries.ArenaEntry;
import net.forthecrown.crownevents.entries.EventEntry;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import org.bukkit.util.BoundingBox;

import java.util.List;

public class ArenaBuilder<T extends EventArena<E>, E extends EventEntry> implements Cloneable {
    private final ArenaConstructor<T, E> constructor;
    private Integer tickInterval;
    private LocOffset boundMin, boundMax;
    private final List<UnbuiltTrigger<E>> unbuiltTriggers = new ObjectArrayList<>();
    private final List<ArenaSectionBuilder> sectionBuilders = new ObjectArrayList<>();

    public ArenaBuilder(ArenaConstructor<T, E> constructor) {
        this.constructor = constructor;
    }

    public ArenaBuilder<T, E> addTrigger(UnbuiltTrigger<E> trigger) {
        this.unbuiltTriggers.add(trigger);
        return this;
    }

    public ArenaBuilder<T, E> setTickUpdates(Integer tickInterval) {
        this.tickInterval = tickInterval;
        return this;
    }

    public ArenaBuilder<T, E> addSection(ArenaSectionBuilder builder) {
        sectionBuilders.add(builder);
        return this;
    }

    public ArenaBuilder<T, E> setBounds(BoundingBox box) {
        this.boundMin = new LocOffset(box.getMinX(), box.getMinY(), box.getMinZ(), 0, 0);
        this.boundMin = new LocOffset(box.getMaxX(), box.getMaxY(), box.getMaxZ(), 0, 0);

        return this;
    }

    public ArenaBuilder<T, E> setBoundMin(LocOffset boundMin) {
        this.boundMin = boundMin;
        return this;
    }

    public ArenaBuilder<T, E> setBoundMax(LocOffset boundMax) {
        this.boundMax = boundMax;
        return this;
    }

    public T build(E entry, ArenaBuildContext context) {
        List<ArenaTrigger<E>> triggers = new ObjectArrayList<>();
        ArenaTickUpdater updater = tickInterval == null ? null : new ArenaTickUpdater(tickInterval);

        for (UnbuiltTrigger<E> t: unbuiltTriggers) {
            triggers.add(t.build(context));
        }

        for (ArenaSectionBuilder b: sectionBuilders) {
            b.build(context);
        }

        T result = constructor.newInstance(
                entry, updater, triggers, createBounds(context)
        );

        if(updater != null) {
            updater.arena = result;
        }

        if(entry instanceof ArenaEntry e) {
            e.setArena(result);
        }

        return result;
    }

    private FtcBoundingBox createBounds(ArenaBuildContext context) {
        if(boundMin == null || boundMax == null) return null;

        WorldVec3i min = boundMin.applyBlock(context.getBuildPosition());
        WorldVec3i max = boundMax.applyBlock(context.getBuildPosition());

        return FtcBoundingBox.of(context.getBuildLocation().getWorld(), min, max);
    }

    @Override
    public ArenaBuilder<T, E> clone() {
        ArenaBuilder<T, E> result = new ArenaBuilder<>(constructor);
        result.tickInterval = tickInterval;
        result.boundMin = this.boundMin;
        result.boundMax = this.boundMax;
        result.sectionBuilders.addAll(this.sectionBuilders);
        result.unbuiltTriggers.addAll(this.unbuiltTriggers);

        return result;
    }
}
