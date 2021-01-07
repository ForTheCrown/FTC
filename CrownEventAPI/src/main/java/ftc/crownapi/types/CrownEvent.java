package ftc.crownapi.types;

import ftc.crownapi.EventApi;
import ftc.crownapi.config.CrownBooleanSettings;
import ftc.crownapi.config.CrownMessages;
import ftc.crownapi.types.interfaces.ICrownEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrownEvent implements ICrownEvent {

    private final EventApi main;

    private FileConfiguration config = EventApi.getInstance().getConfig();
    private ConfigurationSection settingSection = config.getConfigurationSection("settings");
    private ConfigurationSection messageSection = config.getConfigurationSection("messages");
    private Location startLocation = config.getLocation("start-location");
    private Location lobbyLocation = config.getLocation("lobby-location");

    public CrownEvent(EventApi main){
        this.main = main;
    }

    @Override
    public Map<Player, Integer> getScoreMap() {
        return main.scoreMap;
    }
    @Override
    public void removeFromScoreMap(Player player) {
        main.scoreMap.remove(player);
    }
    @Override
    public void setScoreMap(Map<Player, Integer> map) {
        main.scoreMap = map;
    }


    @Override
    public List<UUID> getPlayersInEvent() {
        return main.playersInEvent;
    }
    @Override
    public void setPlayersInEvent(List<UUID> list) {
        main.playersInEvent = list;
    }


    @Override
    public List<UUID> getDisqualifiedPlayers() {
        return main.disqualifiedPlayersList;
    }
    @Override
    public void setDisqualifiedPlayers(List<UUID> list) {
        main.disqualifiedPlayersList = list;
    }


    @Override
    public List<UUID> getPlayersThatQuitInEvent() {
        return main.quitPlayersList;
    }
    @Override
    public void setPlayersThatQuitInEventList(List<UUID> list) {
        main.disqualifiedPlayersList = list;
    }

    @Override
    public Objective getCrownObjective() {
        return getScoreboard().getObjective("crown");
    }

    @Override
    public Scoreboard getScoreboard() {
        return main.getServer().getScoreboardManager().getMainScoreboard();
    }

    @Override
    public StringBuilder getTimerString(long time) {
        long minutes = (time / 60000) % 60;
        long seconds = (time / 1000) % 60;
        long milliseconds = (time/100 ) % 100;

        StringBuilder message = new StringBuilder("Timer: ");
        message.append(String.format("%02d", minutes)).append(":");
        message.append(String.format("%02d", seconds)).append(":");
        message.append(String.format("%02d", milliseconds));
        return message;
    }

    public ConfigurationSection getSettingSection() {
        return settingSection;
    }
    public ConfigurationSection getMessageSection(){
        return messageSection;
    }

    public boolean getSetting(CrownBooleanSettings setting){
        String stringSetting = setting.toString().replace('_', '-');
        stringSetting = stringSetting.toLowerCase();
        return getSettingSection().getBoolean(stringSetting);
    }
    public void setSetting(CrownBooleanSettings setting, boolean value){
        String stringSetting = setting.toString().replace('_', '-');
        getSettingSection().set(stringSetting.toLowerCase(), value);
    }

    public String getMessage(CrownMessages message){
        String stringMessage = message.toString().replace('_', '-');
        stringMessage = stringMessage.toLowerCase();
        String string = getMessageSection().getString(stringMessage);
        string = getMessageSection().getString("prefix") + string;
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public Location getStartLocation(){
        return startLocation;
    }
    public Location getLobbyLocation(){
        return lobbyLocation;
    }
    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }
    public void reloadSettings() {
        main.reloadConfig();
        config = main.getConfig();
        settingSection = config.getConfigurationSection("settings");
        messageSection = config.getConfigurationSection("messages");
        lobbyLocation = config.getLocation("lobby-location");
        startLocation = config.getLocation("start-location");
    }
    public void saveSettings() {
        main.getConfig().set("settings", getSettingSection());
        main.getConfig().set("messages", getMessageSection());
        main.getConfig().set("start-location", getStartLocation());
        main.getConfig().set("lobby-location", getLobbyLocation());
        main.saveConfig();
    }
}