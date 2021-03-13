package net.forthecrown.core.crownevents;

import org.bukkit.Location;
import org.bukkit.scoreboard.Objective;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveLeaderboard extends ArmorStandLeaderboard{

    private Objective objective;

    public ObjectiveLeaderboard(String title, Objective list, Location location) {
        super(title, convertToMap(list), location);

        this.objective = list;
    }

    public static Map<String, Integer> convertToMap(Objective objective){
        Map<String, Integer> tempMap = new HashMap<>();

        for (String s: objective.getScoreboard().getEntries()){
            tempMap.put(s, objective.getScore(s).getScore());
        }
        return tempMap;
    }

    @Override
    public void create() {
        setList(convertToMap(getObjective()));
        super.create();
    }

    public Objective getObjective() {
        return objective;
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }
}
