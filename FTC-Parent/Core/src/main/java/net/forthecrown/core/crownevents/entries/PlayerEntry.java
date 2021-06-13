package net.forthecrown.core.crownevents.entries;

import net.forthecrown.core.crownevents.InEventListener;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserManager;
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
