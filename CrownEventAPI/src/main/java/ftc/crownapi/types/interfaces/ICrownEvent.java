package ftc.crownapi.types.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ICrownEvent {

    Map<Player, Integer> getScoreMap();
    void removeFromScoreMap(Player player);
    void setScoreMap(Map<Player, Integer> map);

    List<UUID> getPlayersInEvent();
    void setPlayersInEvent(List<UUID> list);

    List<UUID> getDisqualifiedPlayers();
    void setDisqualifiedPlayers(List<UUID> list);

    List<UUID> getPlayersThatQuitInEvent();
    void setPlayersThatQuitInEventList(List<UUID> list);

    Objective getCrownObjective();
    Scoreboard getScoreboard();

    StringBuilder getTimerString(long time);
}