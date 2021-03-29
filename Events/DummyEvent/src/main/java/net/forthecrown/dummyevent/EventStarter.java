package net.forthecrown.dummyevent;

import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.crownevents.entries.TimerEntry;
import net.forthecrown.dummyevent.events.InEventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.time.Duration;

public class EventStarter {

    private final SprintEvent event;

    public EventStarter(Player player, int path, SprintEvent event){
        this.event = event;

        player.getActivePotionEffects().clear();
        player.getInventory().clear();
        countdown(player, path);
    }

    int loopID = 0;
    int secondOn = 5;
    private void countdown(Player player, int path){
        secondOn = 5;
        loopID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SprintMain.plugin, () -> {
            Title title = Title.title(
                    Component.text(secondOn < 1 ? "Go!" : secondOn + "").color(NamedTextColor.YELLOW),
                    Component.text("Get ready to run a lap!"),
                    Title.Times.of(Duration.ofMillis(250), Duration.ofMillis(500), Duration.ofMillis(250))
            );
            player.showTitle(title);

            if(secondOn < 1){
                player.setWalkSpeed(0.2F);//Set back to default
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 2.0f, 1.2f);

                EventTimer timer = new EventTimer(player, plr -> {
                    plr.sendMessage("Too slow :p");
                    event.end(SprintEvent.PARTICIPANTS.get(player));
                });
                InEventListener inEventListener = new InEventListener(player);
                TimerEntry entry = new TimerEntry(player, inEventListener,  timer);
                SprintEvent.AVAILABLE_ROWS.set(path, false);
                inEventListener.entry = entry;

                event.setBarrierWall(Material.AIR);

                timer.start(5);
                SprintEvent.PARTICIPANTS.put(player, entry);
                Bukkit.getScheduler().cancelTask(loopID);
                return;
            } else player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2.0f, 2.0f);
            secondOn--;
        }, 0, 20);
    }
}
