package net.forthecrown.julyevent;

import net.forthecrown.core.crownevents.CrownEvent;
import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.utils.CrownBoundingBox;
import net.forthecrown.julyevent.items.EventItemKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CoolParkourEvent implements CrownEvent<JulyEntry> {

    public static final World EVENT_WORLD = Objects.requireNonNull(Bukkit.getWorld("world_july_event"));
    public static final Objective CROWN = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("crown");

    public static final Location LOBBY      = new Location(EVENT_WORLD, -34.5, 74, 194.5, 180, 0);
    public static final Location PRACTISE   = new Location(EVENT_WORLD, -34.5, 94, 125.5, 180, 0);

    public static final CrownBoundingBox REGION = new CrownBoundingBox(EVENT_WORLD, 0, 70, 130, -225, 256, -238);

    public static final BitSet IN_USE_TRACKS = new BitSet();
    public static final Map<Player, JulyEntry> ENTRIES = new HashMap<>();

    @Override
    public void start(Player player) {
        if(cannotEnter(player)) return;

        JulyMain.eLogger.logEntry(player);
        new EventBuilder(player)
                .buildTrack()
                .placeGems()
                .build();
    }

    public void startPractise(Player player){
        if(cannotEnter(player)) return;

        player.teleport(PRACTISE);
        player.getInventory().clear();

        JulyMain.eLogger.logEntry(player);
        EventItemKit.give(player);
    }

    @Override
    public void end(JulyEntry entry) {
        ENTRIES.remove(entry.player());
        entry.player().getInventory().clear();
        entry.player().teleport(LOBBY);
        entry.inEventListener().unregister();

        EventTimer timer = entry.timer();
        if(!timer.wasStopped()) timer.stop();

        IN_USE_TRACKS.set(entry.bitSetIndex(), false);
        JulyMain.leaderboard.update();
        JulyMain.eLogger.logExit(entry.player(), (int) timer.getTime());
    }

    @Override
    public void complete(JulyEntry entry) {
        CrownUser user = entry.user();
        Score scr = CROWN.getScore(user.getName());

        //Fuck using the timer time, that can somehow be delayed by the client, this is exact.
        entry.endTime = System.currentTimeMillis();
        long timeTaken = entry.endTime() - entry.startTime();

        TextComponent.Builder message = Component.text();

        if(!scr.isScoreSet() || scr.getScore() < timeTaken){
            scr.setScore((int) timeTaken);
            message.append(Component.text("New high score!").style(Style.style(NamedTextColor.AQUA, TextDecoration.BOLD)));
        } else message.append(Component.text("Better luck next time.").color(NamedTextColor.YELLOW));

        user.sendMessage(
                message
                        .append(Component.text("Time: ").color(NamedTextColor.GRAY))
                        .append(Component.text(EventTimer.getTimerCounter(timeTaken).toString()))
        );
        end(entry);
    }

    public boolean cannotEnter(Player player){
        if(ENTRIES.containsKey(player)){
            player.sendMessage(Component.text("You're already in the event, wat").color(NamedTextColor.RED));
            return true;
        }

        if(!player.getInventory().isEmpty()){
            player.sendMessage(Component.text("Your inventory must be empty to enter").color(NamedTextColor.RED));
            return true;
        }

        return false;
    }
}
