package net.forthecrown.core;

public interface MiniGameRegion {
    static int chickenLevitation(){
        return ComVars.chickenLevitation.getValue(5);
    }

    static int chickenLevitationTime(){
        return ComVars.chickenLevitationTime.getValue(10);
    }

}
