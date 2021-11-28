package net.forthecrown.crown;

import org.bukkit.scoreboard.Score;

public final class CrownEventUtils {
    private CrownEventUtils() {}

    public static boolean isNewRecord(Score record, int score){
        int recordInt = record.getScore();
        return !record.isScoreSet() || score > recordInt;
    }
}