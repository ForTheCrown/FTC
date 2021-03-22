package net.forthecrown.easteregghunt;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.crownevents.types.CrownEvent;
import net.forthecrown.easteregghunt.events.InEventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.time.Duration;

public class EasterEvent implements CrownEvent<EasterEntry> {

    private final EasterMain main;
    private final EggSpawner spawner;

    public static final Objective CROWN = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("crown");
    public static final Location eventLocation = new Location(Bukkit.getWorld("world_void"), -623, 106, 266);
    public EasterEntry entry;

    public EasterEvent(EasterMain main, EggSpawner spawner){
        this.main = main;
        this.spawner = spawner;
    }

    @Override
    public void start(Player player) {
        spawner.placeEggs();

        doEntryCountdown(player);
    }

    @Override
    public void end(EasterEntry entry) {
        Score record = CROWN.getScore(entry.player().getName());
        CrownUser user = entry.user();

        if(record.isScoreSet() || record.getScore() < entry.score()){
            record.setScore(entry.score());

            IChatBaseComponent text = new ChatComponentText("New record! ").a(EnumChatFormat.YELLOW)
                    .addSibling(new ChatComponentText("Score: " + entry.score()).a(EnumChatFormat.GOLD));

            user.sendMessage(text, ChatMessageType.SYSTEM);
            user.sendMessage(text, ChatMessageType.CHAT);
        } else user.sendMessage(new ChatComponentText("Better luck next time").a(EnumChatFormat.GRAY));

        HandlerList.unregisterAll(entry.inEventListener());
        this.entry = null;
    }

    @Override
    public void complete(EasterEntry timerEntry) {
        end(timerEntry);
    }

    private int loopID = 0;
    private int secondOn = 5;
    private void doEntryCountdown(Player player){
        secondOn = 5;

        loopID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            final boolean shouldStart = secondOn < 1;

            Title title = Title.title(
                    Component.text(shouldStart ? "Go!" : secondOn + "").color(NamedTextColor.YELLOW),
                    Component.text("Get ready!").color(NamedTextColor.GOLD),
                    Title.Times.of(Duration.ofMillis(250), Duration.ofMillis(500), Duration.ofMillis(250))
            );
            player.showTitle(title);

            InEventListener listener = new InEventListener();
            if(shouldStart){
                entry = new EasterEntry(player, listener, new EventTimer(player, plr -> end(entry)));
                listener.entry = entry;
                listener.event = this;

                EasterMain.instance.getServer().getPluginManager().registerEvents(entry.inEventListener(), EasterMain.instance);

                player.teleport(eventLocation);
                Bukkit.getScheduler().cancelTask(loopID);
                return;
            }

            secondOn--;
        }, 0, 20);
    }

}
