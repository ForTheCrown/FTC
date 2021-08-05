package net.forthecrown.crownevents;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public final class CrownEventUtils {
    private CrownEventUtils() {}

    public static Objective crownObjective() {
        return Bukkit.getScoreboardManager().getMainScoreboard().getObjective("crown");
    }

    public static boolean isNewRecord(Score record, int score){
        int recordInt = record.getScore();
        return !record.isScoreSet() || score > recordInt;
    }
}