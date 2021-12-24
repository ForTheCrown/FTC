package net.forthecrown.inventory.weapon.click;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.events.WeaponListener;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

public class ClickHistory {
    public static final int HISTORY_CLEAR_TIME_TICKS = 2 * 20;

    public final UUID playerId;
    public final List<Click> clicks = new ObjectArrayList<>();

    private BukkitTask clearTask;

    public ClickHistory(UUID playerId) {
        this.playerId = playerId;
    }

    public boolean isOfLength(int length) {
        return clicks.size() >= length;
    }

    public void taskLogic() {
        if(clearTask != null && !clearTask.isCancelled()) {
            clearTask.cancel();
        }

        clearTask = Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
            clicks.clear();
            clearTask = null;

            WeaponListener.CLICK_HISTORIES.remove(playerId);
        }, HISTORY_CLEAR_TIME_TICKS);
    }

    public boolean hasPattern(Click... pattern) {
        Validate.noNullElements(pattern);
        if(!isOfLength(pattern.length)) return false;

        for (int i = 0; i < pattern.length; i++) {
            Click type = pattern[i];
            Click compare = clicks.get(i);

            if(type != compare) return false;
        }

        return true;
    }
}
