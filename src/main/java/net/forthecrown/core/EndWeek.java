package net.forthecrown.core;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import static net.kyori.adventure.text.Component.text;

public class EndWeek {
    public static final String OBJ_NAME = "end_week";

    @Getter
    private static final EndWeek instance = new EndWeek();

    public Objective getObjective() {
        var scoreboard = Bukkit.getScoreboardManager()
                .getMainScoreboard();

        var obj = scoreboard.getObjective(OBJ_NAME);

        if (obj == null) {
            obj = scoreboard.registerNewObjective(
                    OBJ_NAME,
                    Criteria.statistic(Statistic.KILL_ENTITY, EntityType.ENDERMAN),
                    text("Endermen killed")
            );
        }

        return obj;
    }

    public void begin() {
        var obj = getObjective();
        obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

    public void close() {
        var scoreboard = Bukkit.getScoreboardManager()
                .getMainScoreboard();

        var obj = getObjective();
        var death = scoreboard.getObjective("Death");

        if (death != null) {
            death.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        var entries = scoreboard.getEntries()
                .stream()
                .map(obj::getScore)
                .filter(Score::isScoreSet)
                .toList();

        throw new IllegalStateException("TODO");
    }
}