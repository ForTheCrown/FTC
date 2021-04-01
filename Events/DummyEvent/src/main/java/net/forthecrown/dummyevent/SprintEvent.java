package net.forthecrown.dummyevent;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.crownevents.entries.TimerEntry;
import net.forthecrown.core.crownevents.types.TimedEvent;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class SprintEvent implements TimedEvent {

    public static final Map<Player, TimerEntry> PARTICIPANTS = new HashMap<>();

    public static final Objective CROWN = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("crown");
    public static final Location EXIT_LOCATION = new Location(Bukkit.getWorld("world_void"), -595, 107, 455);
    public static final Location RACE_LOCATION = new Location(Bukkit.getWorld("world_void"), -551.5, 106, 476, 90, 0);
    public static final CrownBoundingBox RACE_AREA = new CrownBoundingBox(Bukkit.getWorld("world_void"), -686, 95, 452, -505, 130, 545);
    public static final BitSet AVAILABLE_ROWS = new BitSet(5);

    @Override
    public void start(Player player){
        byte startLoc = findAvailableRow();
        if(startLoc == -1){
            player.sendMessage("Race is currently full, come back later :)");
            return;
        }

        player.teleport(RACE_LOCATION.clone().subtract(0, 0,startLoc*3));
        player.setWalkSpeed(0);//Set to 0 for countdown thing
        setBarrierWall(Material.BARRIER, startLoc);
        new EventStarter(player, startLoc, this);
    }

    public byte findAvailableRow(){
        for (byte i = 0; i < 5; i++){
            if(AVAILABLE_ROWS.get(i)) continue;

            AVAILABLE_ROWS.set(i, true);
            return i;
        }
        return -1;
    }

    public void setBarrierWall(Material material, byte row){
        Location location = new Location(Bukkit.getWorld("world_void"), -553, 107, 477 - (row * 3));

        location.getBlock().setType(material);
        location.add(1, 0, 0).getBlock().setType(material);
        location.add(1, 0, 0).getBlock().setType(material);
        location.subtract(2, 0, 1).getBlock().setType(material);
        location.subtract(0, 0, 1).getBlock().setType(material);
        location.subtract(0, 0, 1).getBlock().setType(material);
        location.add(1, 0, 0).getBlock().setType(material);
        location.add(1, 0, 0).getBlock().setType(material);
    }

    public void end(TimerEntry entry){
        HandlerList.unregisterAll(entry.inEventListener());
        EventTimer timer = entry.timer();
        if(!timer.wasStopped()) timer.stop();
        entry.player().teleport(EXIT_LOCATION);
    }

    public void endAndRemove(TimerEntry entry){
        end(entry);
        PARTICIPANTS.remove(entry.player());
    }

    public boolean isInEvent(Player player){
        return PARTICIPANTS.containsKey(player);
    }

    public void clear(){
        for (Player p: PARTICIPANTS.keySet()){
            TimerEntry entry = PARTICIPANTS.get(p);
            end(entry);
        }
        PARTICIPANTS.clear();
    }

    public void complete(TimerEntry entry){
        EventTimer timer = entry.timer();
        Score playerScore = CROWN.getScore(entry.player().getName());
        CrownUser u = FtcCore.getUser(entry.player());
        IChatBaseComponent timeText = new ChatComponentText("Time: " + EventTimer.getTimerCounter(timer.getTime()).toString())
                .a(EnumChatFormat.GOLD);
        IChatMutableComponent text;

        //if better score lol
        if(!playerScore.isScoreSet() || playerScore.getScore() > timer.getTime() || playerScore.getScore() == 0){
            text = new ChatComponentText("New record! ")
                    .a(EnumChatFormat.YELLOW);
            playerScore.setScore((int) timer.getTime());
            entry.player().getWorld().playSound(entry.player().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);

        } else {
            text = new ChatComponentText("Better luck next time! ")
                    .a(EnumChatFormat.YELLOW);
        }

        text.addSibling(timeText);
        u.sendMessage(text, ChatMessageType.GAME_INFO);
        u.sendMessage(text, ChatMessageType.CHAT);
        SprintMain.leaderboard.update();
        endAndRemove(entry);
    }
}
