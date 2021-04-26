package net.forthecrown.core.crownevents.entries;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.crownevents.InEventListener;
import org.bukkit.entity.Player;

public class PlayerEntry<T extends PlayerEntry<T>> extends EventEntry<T> {

    protected final Player entry;
    protected final CrownUser user;

    public PlayerEntry(Player entry, InEventListener<T> inEventListener) {
        super(inEventListener);
        this.entry = entry;
        this.user = UserManager.getUser(entry);
    }

    public Player player() {
        return entry;
    }

    public CrownUser user(){
        return user;
    }
}
