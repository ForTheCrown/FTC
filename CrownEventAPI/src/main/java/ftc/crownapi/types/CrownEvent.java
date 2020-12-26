package ftc.crownapi.types;

import ftc.crownapi.Main;
import ftc.crownapi.types.interfaces.ICrownEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Map;

public class CrownEvent implements ICrownEvent {

    @Override
    public Map<Player, Integer> getScoreMap() {
        return Main.plugin.scoreMap;
    }
    @Override
    public void removeFromScoreMap(Player player) {
        Main.plugin.scoreMap.remove(player);
    }
    @Override
    public void setScoreMap(Map<Player, Integer> map) {
        Main.plugin.scoreMap = map;
    }


    @Override
    public List<Player> getPlayersInEvent() {
        return Main.plugin.playersInEvent;
    }
    @Override
    public void setPlayersInEvent(List<Player> list) {
        Main.plugin.playersInEvent = list;
    }


    @Override
    public List<Player> getDisqualifiedPlayers() {
        return Main.plugin.disqualifiedPlayersList;
    }
    @Override
    public void setDisqualifiedPlayers(List<Player> list) {
        Main.plugin.disqualifiedPlayersList = list;
    }


    @Override
    public List<Player> getPlayersThatQuitInEvent() {
        return Main.plugin.quitPlayersList;
    }
    @Override
    public void setPlayersThatQuitInEventList(List<Player> list) {
        Main.plugin.disqualifiedPlayersList = list;
    }

    @Override
    public Objective getCrownObjective() {
        return getScoreboard().getObjective("crown");
    }

    @Override
    public Scoreboard getScoreboard() {
        return Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
    }


    @Override
    public Location getStartLocation() {
        return Main.plugin.getConfig().getLocation("start-location");
    }
    @Override
    public void setStartLocation(Location location) {
        Main.plugin.getConfig().set("start-location", location);
        Main.plugin.saveConfig();
    }


    @Override
    public Location getLobbyLocation() {
        return Main.plugin.getConfig().getLocation("lobby-location");
    }
    @Override
    public void setLobbyLocation(Location location) {
        Main.plugin.getConfig().set("lobby-location", location);
        Main.plugin.saveConfig();
    }
}
