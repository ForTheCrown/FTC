package net.forthecrown.core.crownevents.entries;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class SingleEntry extends EventEntry{

    protected final Player entry;

    public SingleEntry(Player entry, Listener inEventListener) {
        super(inEventListener);
        this.entry = entry;
    }

    public Player player() {
        return entry;
    }

    public CrownUser user(){
        return FtcCore.getUser(entry);
    }
}
