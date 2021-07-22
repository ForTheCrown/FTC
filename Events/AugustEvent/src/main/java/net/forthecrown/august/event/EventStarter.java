package net.forthecrown.august.event;

import net.forthecrown.august.AugustPlugin;
import net.forthecrown.august.EventConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class EventStarter {

    private final Player player;
    private final AugustEntry entry;

    public EventStarter(Player player) {
        this.player = player;
        this.entry = new AugustEntry(player);

        countdownID = countDown();
    }

    private final int countdownID;
    private byte secondOn = 3;

    private int countDown() {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(AugustPlugin.inst, () -> {
            secondOn--;
            boolean last = secondOn < 0;

            if(last) {
                player.teleport(EventConstants.START);
                entry.timer().startTickingDown(EventConstants.MAX_TICKS_IN_EVENT);
                entry.inEventListener().register(AugustPlugin.inst);

                player.getInventory().removeItemAnySlot(EventConstants.ticket());

                PinataEvent.currentEntry = entry;
                cancel();
            }

            Component titleC = Component.text(last ? "Go!" : ("" + secondOn)).color(NamedTextColor.YELLOW);
            Component subTitle = Component.text("Get ready!").color(NamedTextColor.GOLD);

            Title.Times times = Title.Times.of(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(100));
            Title title = Title.title(titleC, subTitle, times);

            player.showTitle(title);
        }, 0, 20);
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(countdownID);
        PinataEvent.currentStarter = null;
    }

    public Player getPlayer() {
        return player;
    }
}
