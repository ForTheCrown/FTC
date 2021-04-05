package net.forthecrown.core.crownevents.entries;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
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
        return UserManager.getUser(entry);
    }
}
