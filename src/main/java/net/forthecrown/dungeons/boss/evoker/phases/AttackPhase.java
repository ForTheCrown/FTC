package net.forthecrown.dungeons.boss.evoker.phases;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.events.Events;
import org.bukkit.boss.BossBar;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * Represents a single phase of the Evoker's attack pattern thingy
 */
public interface AttackPhase extends Listener {
    /**
     * Starts the phase
     * @param boss The boss
     */
    default void start(EvokerBoss boss) {
        // Update phase progress bar
        boss.updatePhaseBarViewers();
        BossBar bar = boss.getPhaseBar();
        bar.setProgress(0d);
        // Other phases might change name, ensure it's the default
        bar.setTitle("Phase progress");

        // Register events and call onStart
        Events.register(this);
        onStart(boss, boss.currentContext());
    }

    /**
     * Ends the phase
     * @param boss The boss
     */
    default void end(EvokerBoss boss) {
        // Make bar invis and unregister event handlers
        boss.getPhaseBar().setVisible(false);
        HandlerList.unregisterAll(this);
        onEnd(boss, boss.currentContext());
    }

    void onStart(EvokerBoss boss, BossContext context);
    void onEnd(EvokerBoss boss, BossContext context);

    default void onTick(EvokerBoss boss, BossContext context) {}
}