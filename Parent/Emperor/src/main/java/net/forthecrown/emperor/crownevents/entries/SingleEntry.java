package net.forthecrown.emperor.crownevents.entries;

import net.forthecrown.emperor.crownevents.InEventListener;
import org.bukkit.entity.Player;

public class SingleEntry extends PlayerEntry<SingleEntry> {
    public SingleEntry(Player entry, InEventListener<SingleEntry> inEventListener) {
        super(entry, inEventListener);
    }
}
