package net.forthecrown.crownevents.engine;

import net.forthecrown.crownevents.entries.EventEntry;
import net.forthecrown.utils.transformation.FtcBoundingBox;

import javax.annotation.Nullable;
import java.util.List;

public interface ArenaConstructor<T extends EventArena<E>, E extends EventEntry> {
    T newInstance(E entry, @Nullable ArenaTickUpdater updater, @Nullable List<ArenaTrigger<E>> triggers, @Nullable FtcBoundingBox arenaBounds);
}
