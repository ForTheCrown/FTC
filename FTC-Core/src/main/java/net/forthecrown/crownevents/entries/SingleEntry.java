package net.forthecrown.crownevents.entries;

import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import org.bukkit.entity.Player;

public abstract class SingleEntry implements EventEntry {

    protected final CrownUser user;
    protected final Player player;

    public SingleEntry(Player player) {
        this.player = player;
        this.user = UserManager.getUser(player);
    }

    public CrownUser user() {
        return user;
    }

    public Player player() {
        return player;
    }
}
