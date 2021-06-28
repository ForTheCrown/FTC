package net.forthecrown.july;

import net.forthecrown.core.crownevents.CrownEvent;
import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.july.items.EventItems;
import net.forthecrown.july.listener.OnTrackListener;
import net.forthecrown.july.rewards.RewardChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Score;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static net.forthecrown.july.EventConstants.*;

public class ParkourEvent implements CrownEvent<ParkourEntry> {

    public static final BitSet IN_USE_TRACKS = new BitSet();
    public static final Map<Player, ParkourEntry> ENTRIES = new HashMap<>();
    public static final Map<Player, EventBuilder> WAITING_FOR_START = new HashMap<>();
    public static final Map<Player, OnTrackListener> PRACTISE_TRACKER = new HashMap<>();

    @Override
    public void start(Player player) {
        if(cannotEnter(player, false)) return;

        player.sendMessage(Component.text("Entering event...").color(NamedTextColor.GRAY));
        JulyMain.reporter.logEntry(player);
        new EventBuilder(player)
                //.buildTrack()
                .placeGems()
                .build();
    }

    public void startPractise(Player player){
        if(cannotEnter(player, true)) return;

        player.teleport(PRACTISE);
        player.getInventory().clear();

        OnTrackListener listener = new OnTrackListener(player, true);
        listener.register(JulyMain.inst);
        listener.calculateOffsets(minLoc());
        PRACTISE_TRACKER.put(player, listener);

        player.sendMessage(Component.text("Entering practise...").color(NamedTextColor.GRAY));
        JulyMain.reporter.logEntry(player);
        EventItems.giveStarter(player);
    }

    public void endPractise(Player player){
        OnTrackListener listener = PRACTISE_TRACKER.get(player);
        listener.unregister();

        player.getInventory().clear();
        player.teleport(LOBBY);

        PRACTISE_TRACKER.remove(player);
        JulyMain.reporter.logExit(player);
    }

    @Override
    public void end(ParkourEntry entry) {
        ENTRIES.remove(entry.player());
        entry.inEventListener().resetTrack();

        entry.player().getInventory().clear();
        entry.player().teleport(LOBBY);

        entry.inEventListener().unregister();

        EventTimer timer = entry.timer();
        if(!timer.wasStopped()) timer.stop();

        IN_USE_TRACKS.set(entry.bitSetIndex(), false);
        JulyMain.updateLb();
        JulyMain.reporter.logExit(entry.player());
    }

    @Override
    public void complete(ParkourEntry entry) {
        CrownUser user = entry.user();
        Score scr = CROWN.getScore(user.getName());
        long timeTaken = System.currentTimeMillis() - entry.startTime();

        TextComponent.Builder message = Component.text();

        if(!scr.isScoreSet() || scr.getScore() > timeTaken){
            if(scr.isScoreSet()){
                EventUtils.spawnFirework(FIREWORK_1, 0, COOL_EFFECT, COOL_END_EFFECT);
                EventUtils.spawnFirework(FIREWORK_2, 0, COOL_EFFECT, COOL_END_EFFECT);
            }

            scr.setScore((int) timeTaken);
            message.append(Component.text("New high score!").style(Style.style(NamedTextColor.AQUA, TextDecoration.BOLD)));
        } else message.append(Component.text("Better luck next time.").color(NamedTextColor.YELLOW));

        RewardChecker.checkNeedsCoolRank(entry.player(), timeTaken);
        JulyMain.reporter.logAction(user.getPlayer(), "Exited the event, time: " + EventTimer.getTimerCounter(timeTaken));

        user.addGems(GEM_COMPLETION);
        user.getPlayer().sendMessage(
                message
                        .append(Component.text(" Time: ").color(NamedTextColor.GRAY))
                        .append(Component.text(EventTimer.getTimerCounter(timeTaken).toString()))
                        .append(Component.newline())
                        .append(Component.text("Got ").color(NamedTextColor.GRAY))
                        .append(Component.text(GEM_COMPLETION + " Gems").color(NamedTextColor.YELLOW))
                        .append(Component.text(" for completing the course!").color(NamedTextColor.GRAY))
                        .build()
        );
        end(entry);
    }

    public boolean cannotEnter(Player player, boolean practise){
        if(ENTRIES.size() >= 10 && practise){
            player.sendMessage(Component.text("Too many people in the event, can't enter."));
            return true;
        }

        if(ENTRIES.containsKey(player) || PRACTISE_TRACKER.containsKey(player)){
            player.sendMessage(Component.text("You're already in the event, wat.").color(NamedTextColor.RED));
            return true;
        }

        if(!practise){
            if(!onlyHasTicket(player.getInventory())){
                player.sendMessage(
                        Component.text("You need to have ")
                                .color(NamedTextColor.RED)
                                .append(Component.text("exactly 1").decorate(TextDecoration.BOLD))
                                .append(Component.text(" ticket."))
                );
                return true;
            } else return false;
        } else if(!player.getInventory().isEmpty()){
            player.sendMessage(Component.text("Your inventory must be empty to enter.").color(NamedTextColor.RED));
            return true;
        }

        return false;
    }

    private boolean onlyHasTicket(PlayerInventory inv){
        boolean foundItem = false;

        for (ItemStack i: inv){
            if(i == null) continue;
            if(!i.equals(EventItems.ticket())) return false;
            if(foundItem) return false;

            foundItem = true;
        }

        return foundItem;
    }
}
