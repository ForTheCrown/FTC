package net.forthecrown.core.crownevents.entries;

import net.forthecrown.core.crownevents.InEventListener;
import org.bukkit.entity.Player;

public class SingleEntry extends PlayerEntry<SingleEntry> {
    public SingleEntry(Player entry, InEventListener<SingleEntry> inEventListener) {
        super(entry, inEventListener);
    }
}
