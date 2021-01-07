package ftc.crownapi.types.interfaces;

import ftc.crownapi.types.CrownEventUser;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

import javax.annotation.Nonnegative;

public interface CrownEventIUser {

    boolean isInEvent();
    void setInEvent(boolean value);

    //tp to server locs methods
    void teleportToHazelguard();
    void teleportToQuestmoor();
    void teleportToKetilheim();

    //admin methods
    void setDisqualified(boolean value);
    boolean isDisqualified();

    //score map methods
    int getScoreMapScore();
    void setScoreMapScore(int score);
    void addToScoreMap();
    void removeFromScoreMap();

    //crown score methods
    Score getCrownScore();
    boolean isRecordSmallerThanScore();

    //event methods
    void teleportToEventLobby();
    void teleportToEventStart();

    //bal methods
    void setBalance(@Nonnegative int amount);
    int getBalance();

    //gems methods
    void setGems(@Nonnegative int amount);
    int getGems();

    boolean hasQuitInEvent();
    void setHasQuitInEvent(boolean value);

    CrownEventUser getUser();
    Player getPlayer();

    //timer methods
    boolean hasTimer();
    void startTimer();
    void stopTimer();
    long getTimerEndTime();

    void startTimerTickingDown(long timeInSeconds);
    long getTimerTickingDownTimeLeft();
}
