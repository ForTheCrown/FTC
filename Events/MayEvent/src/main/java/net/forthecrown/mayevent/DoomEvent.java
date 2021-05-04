package net.forthecrown.mayevent;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.crownevents.types.CrownEvent;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.mayevent.arena.ArenaBuilder;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import net.minecraft.server.v1_16_R3.IChatMutableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public interface DoomEvent extends CrownEvent<ArenaEntry> {

    World EVENT_WORLD = Objects.requireNonNull(Bukkit.getWorld("world_event"));
    Objective CROWN = Objects.requireNonNull(Bukkit.getScoreboardManager().getMainScoreboard().getObjective("crown"));
    Team MOB_TEAM = ((Supplier<Team>) () -> {
        Scoreboard mainScr = Bukkit.getScoreboardManager().getMainScoreboard();
        Team mobTeam = mainScr.getTeam("mobTeam");
        if (mobTeam == null){
            mobTeam = mainScr.registerNewTeam("mobTeam");
            mobTeam.setAllowFriendlyFire(false);
        }

        return mobTeam;
    }).get();

    Location EXIT_LOCATION = new Location(CrownUtils.WORLD, -61.5, 70, 884.5, -90, 0);

    Map<Player, ArenaEntry> ENTRIES = new HashMap<>();
    BitSet IN_USE_ARENAS = new BitSet();

    @Override
    default void start(Player player) {
        try {
            if(!checkEntryConditions(player)) return;
        } catch (CrownException e) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(MayMain.inst, () -> {
            ArenaEntry entry = new ArenaEntry(player);
            entry.arena().start();
            entry.regEvents(MayMain.inst);

            MayMain.eLogger.logEntry(player);
            ENTRIES.put(player, entry);
        });
    }

    @Override
    default void end(ArenaEntry entry) {
        entry.inventory().clear();
        entry.player().teleport(EXIT_LOCATION);

        entry.player().getActivePotionEffects().clear();
        entry.arena().shutdown();
        entry.unregEvents();

        MayMain.leaderboard.update();
        int index = entry.arena().minLoc().getBlockZ() / ArenaBuilder.DISTANCE_BETWEEN;
        IN_USE_ARENAS.set(index, false);

        MayMain.eLogger.logExit(entry.player());
        ENTRIES.remove(entry.player());
    }

    default void complete(ArenaEntry entry){
        CrownUser user = entry.user();
        int wave = entry.arena().wave();
        Score score = CROWN.getScore(entry.player().getName());

        IChatMutableComponent message;

        if(!score.isScoreSet() || score.getScore() < wave){
            message = new ChatComponentText("New high score: ")
                    .a(EnumChatFormat.YELLOW)
                    .addSibling(new ChatComponentText(wave + "").a(EnumChatFormat.GOLD));
            score.setScore(wave);
        } else {
            message = new ChatComponentText("Better luck next time!")
                    .a(EnumChatFormat.GRAY)
                    .addSibling(new ChatComponentText(" Score: " + wave).a(EnumChatFormat.GOLD));
        }

        MayMain.eLogger.logExit(entry.player(), score.getScore());
        user.sendMessage(message);
        Bukkit.getScheduler().runTaskLater(MayMain.inst, () -> end(entry), 1);
    }

    @Override
    default String getName() {
        return "DoomEvent";
    }

    //I'm just fucking around with interfaces at this point lmao
    class Impl implements DoomEvent {}

    static boolean checkEntryConditions(Player player) throws CrownException {
        if(!player.getInventory().isEmpty()) throw new CrownException(player, "&4You need to have an empty inventory to enter");
        if(!player.getActivePotionEffects().isEmpty()) throw new CrownException(player, "You can't have any active potion effects");
        return true;
    }
}
