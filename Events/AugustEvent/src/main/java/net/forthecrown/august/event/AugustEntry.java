package net.forthecrown.august.event;

import net.forthecrown.august.AugustPlugin;
import net.forthecrown.august.EventConstants;
import net.forthecrown.august.listener.AugustInEventListener;
import net.forthecrown.crownevents.EventTimer;
import net.forthecrown.crownevents.entries.SingleEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class AugustEntry extends SingleEntry {

    private final AugustInEventListener listener;
    private final EventTimer timer;

    private int score;

    public AugustEntry(Player player) {
        super(player);

        this.listener = new AugustInEventListener(this, player);
        this.timer = new EventTimer(player, EventConstants.TIMER_FORMAT, plr -> AugustPlugin.event.complete(PinataEvent.currentEntry));
    }

    public EventTimer timer() {
        return timer;
    }

    public void addSecToTimer(int seconds, boolean tell) {
        timer().setTimeInTicks(timer.getTimeInTicks() + seconds * 20L);
        if(tell) player.sendMessage(Component.text("+" + seconds + " seconds!").color(NamedTextColor.GOLD));
    }

    public int score() {
        return score;
    }

    public void increment(int inc) {
        score += inc;
    }

    @Override
    public AugustInEventListener inEventListener() {
        return listener;
    }
}
