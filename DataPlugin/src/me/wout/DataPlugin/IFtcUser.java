package me.wout.DataPlugin;

import java.util.List;

public interface IFtcUser {

    List<String> getKnightRanks();
    void setKnightRanks(List<String> knightRanks);

    List<String> getPirateRanks();
    void setPirateRanks(List<String> pirateRanks);

    String getCurrentRank();
    void setCurrentRank(String rank);

    boolean canSwapBranch();
    void setCanSwapBranch(boolean value);

    List<String> getPets();
    void setPets(List<String> pets);


}
