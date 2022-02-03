package net.forthecrown.core.goalbook;

import net.forthecrown.core.Crown;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.UUID;

public abstract class GoalBookListener implements Listener {
    protected final GoalBookChallenge challenge;

    public GoalBookListener(GoalBookChallenge challenge) {
        this.challenge = challenge;
    }

    public GoalBookChallenge getChallenge() {
        return challenge;
    }

    protected final GoalBook.Progress getProgress(UUID uuid) {
        return Crown.getGoalBook().getProgress(uuid);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, Crown.inst());
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }
}
