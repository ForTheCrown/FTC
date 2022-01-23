package net.forthecrown.crownevents.engine;

import net.forthecrown.crownevents.entries.EventEntry;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;

public abstract class EventArena<E extends EventEntry> {
    protected final ArenaTickUpdater tickUpdater;
    protected final List<ArenaTrigger<E>> triggers;
    protected final E entry;
    protected final FtcBoundingBox arenaBounds;

    public EventArena(E entry, @Nullable ArenaTickUpdater tickUpdater, @Nullable List<ArenaTrigger<E>> triggers, FtcBoundingBox bounds) {
        this.tickUpdater = tickUpdater;
        this.triggers = triggers;
        this.entry = entry;
        arenaBounds = bounds;
    }

    public E getEntry() {
        return entry;
    }

    public boolean checkOutside(Player player) {
        if(arenaBounds == null) return false;
        return arenaBounds.contains(player);
    }

    public void tpIfOutside(Player player, Location dest) {
        if(!checkOutside(player)) return;
        player.teleport(dest);
    }

    public boolean runTriggers(Player player, E entry) {
        if(triggers == null || triggers.isEmpty()) return false;

        boolean result = false;

        for (ArenaTrigger<E> t: triggers) {
            if(t.testAndRun(player, entry)) result = true;
        }

        return result;
    }

    public final void open() {
        if(tickUpdater != null) tickUpdater.start();

        onOpen();
    }

    public final void close() {
        if(tickUpdater != null) tickUpdater.stop();

        onClose();
    }

    public void onUpdate() {}
    protected void onOpen() {}
    protected void onClose() {}
}
