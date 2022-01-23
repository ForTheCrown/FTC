package net.forthecrown.crownevents.engine;

import net.forthecrown.crownevents.entries.EventEntry;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public record ArenaTrigger<E extends EventEntry>(BoundingBox triggerArea, TriggerAction<E> action) {
    public boolean testAndRun(Player player, E entry) {
        if(!triggerArea.contains(player.getLocation().toVector())) return false;

        action.run(player, entry);
        return true;
    }
}
