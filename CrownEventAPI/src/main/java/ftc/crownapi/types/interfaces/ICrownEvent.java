package ftc.crownapi.types.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Map;

public interface ICrownEvent {

    Map<Player, Integer> getScoreMap();
    void removeFromScoreMap(Player player);
    void setScoreMap(Map<Player, Integer> map);

    List<Player> getPlayersInEvent();
    void setPlayersInEvent(List<Player> list);

    List<Player> getDisqualifiedPlayers();
    void setDisqualifiedPlayers(List<Player> list);

    List<Player> getPlayersThatQuitInEvent();
    void setPlayersThatQuitInEventList(List<Player> list);

    Objective getCrownObjective();
    Scoreboard getScoreboard();

    Location getStartLocation();
    void setStartLocation(Location location);

    Location getLobbyLocation();
    void setLobbyLocation(Location location);
}