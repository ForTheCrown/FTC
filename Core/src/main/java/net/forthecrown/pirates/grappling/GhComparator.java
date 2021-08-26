package net.forthecrown.pirates.grappling;

import java.util.Comparator;

public class GhComparator implements Comparator<GhLevelData> {
    @Override
    public int compare(GhLevelData o1, GhLevelData o2) {
        if(o1.getNextLevel() == null) return -1;
        if(o1.getNextLevel().equalsIgnoreCase(o2.getName())) return -1;
        return 1;
    }
}
