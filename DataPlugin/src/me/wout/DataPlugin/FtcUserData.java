package me.wout.DataPlugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class FtcUserData {

    private final String uuid;
    private File file;
    private FileConfiguration fileConfig;

    // If the person has a legacy file, it'll transfer the data from the config.yml to the player data file.
    private boolean legacyData;

    //Already in the file, added by Wout
    private String playerName;
    private List<String> knightRanks;
    private List<String> pirateRanks;
    private String currentRank;
    private boolean canSwapBranch;
    private String activeBranch;
    private List<String> pets;
    private String particleArrowActive;
    private List<String> particleArrowAvailable;
    private String particleDeathActive;
    private List<String> particleDeathAvailable;
    private List<String> emotesAvailable;
    private boolean allowsRidingPlayers;
    private int gems;

    //Added by Botul
    private boolean allowsEmotes;
    private int balance;

    private final UUID playerLink;

    //constructors
    public FtcUserData(Player base){
        uuid = base.getUniqueId().toString();
        playerLink = base.getUniqueId();
        loadUserData();
    }
    public FtcUserData(UUID base){
        uuid = base.toString();
        playerLink = base;
        loadUserData();
    }

    //file methods
    public void reloadUserData(){
        fileConfig = YamlConfiguration.loadConfiguration(file);
        playerName = getUserData().getString("PlayerName");
        knightRanks = getUserData().getStringList("KnightRanks");
        pirateRanks = getUserData().getStringList("PirateRanks");
        currentRank = getUserData().getString("CurrentRank");
        canSwapBranch = getUserData().getBoolean("CanSwapBranch");
        pets = getUserData().getStringList("Pets");
        particleArrowActive = getUserData().getString("ParticleArrowActive");
        particleArrowAvailable = getUserData().getStringList("ParticleArrowAvailable");
        particleDeathActive = getUserData().getString("ParticleDeathActive");
        particleDeathAvailable = getUserData().getStringList("ParticleDeathAvailable");
        emotesAvailable = getUserData().getStringList("EmotesAvailable");
        allowsRidingPlayers = getUserData().getBoolean("AllowsRidingPlayers");
        gems = getUserData().getInt("Gems");
        balance = getUserData().getInt("Balance");
        allowsEmotes = getUserData().getBoolean("AllowsEmotes");
    }
    private void loadUserData(){
        FtcDataMain.plugin.loadedPlayerDatas.add(this);
        file = new File(FtcDataMain.plugin.getDataFolder() + "/playerdata", uuid + ".yml");
        if(!file.exists()){
            legacyData = true;
            legacyUpdate();
            try {
                file.createNewFile();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        reloadUserData();
    }
    public FileConfiguration getUserData(){
        return fileConfig;
    }

    public void saveUserData(){
        getUserData().set("PlayerName", getPlayerName());
        getUserData().set("KnightRanks", getKnightRanks());
        getUserData().set("PirateRanks", getPirateRanks());
        getUserData().set("CurrentRank", getCurrentRank());
        getUserData().set("CanSwapBranch", getCanSwapBranch());
        getUserData().set("ActiveBranch", getActiveBranch());
        getUserData().set("Pets", getPets());
        getUserData().set("ParticleArrowActive", getParticleArrowActive());
        getUserData().set("ParticleArrowAvailable", getParticleArrowAvailable());
        getUserData().set("ParticleDeathActive", getParticleDeathActive());
        getUserData().set("ParticleDeathAvailable", getParticleDeathAvailable());
        getUserData().set("EmotesAvailable", getEmotesAvailable());
        getUserData().set("AllowsRidingPlayers", getAllowsRidingPlayers());
        getUserData().set("Gems", getGems());
        getUserData().set("Balance", getBalance());
        getUserData().set("AllowsEmotes", getAllowsEmotes());

        try {
            fileConfig.save(file);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Takes the data from the config.yml and places it into the proper player data file
    private void legacyUpdate(){
        if (!legacyData) return;

        ConfigurationSection oldData = FtcDataMain.plugin.getConfig().getConfigurationSection("players." + uuid);
        setPlayerName(oldData.getString("PlayerName"));
        setKnightRanks(oldData.getStringList("KnightRanks"));
        setPirateRanks(oldData.getStringList("PirateRanks"));
        setCurrentRank(oldData.getString("CurrentRank"));
        setCanSwapBranch(oldData.getBoolean("CanSwapBranch"));
        setActiveBranch(oldData.getString("ActiveBranch"));
        setPets(oldData.getStringList("Pets"));
        setParticleArrowActive(oldData.getString("ParticleArrowActive"));
        setParticleArrowAvailable(oldData.getStringList("ParticleArrowAvailable"));
        setParticleDeathActive(oldData.getString("ParticleDeathActive"));
        setParticleDeathAvailable(oldData.getStringList("ParticleDeathAvailable"));
        setEmotesAvailable(oldData.getStringList("EmotesAvailable"));
        setAllowsRidingPlayers(oldData.getBoolean("AllowsRidingPlayers"));
        setGems(oldData.getInt("Gems"));
    }


    //getters and setters
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public List<String> getKnightRanks() {
        return knightRanks;
    }

    public void setKnightRanks(List<String> knightRanks) {
        this.knightRanks = knightRanks;
    }

    public List<String> getPirateRanks() {
        return pirateRanks;
    }

    public void setPirateRanks(List<String> pirateRanks) {
        this.pirateRanks = pirateRanks;
    }

    public String getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(String currentRank) {
        this.currentRank = currentRank;
    }

    public boolean getCanSwapBranch() {
        return canSwapBranch;
    }

    public void setCanSwapBranch(boolean canSwapBranch) {
        this.canSwapBranch = canSwapBranch;
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public void setActiveBranch(String activeBranch) {
        this.activeBranch = activeBranch;
    }

    public List<String> getPets() {
        return pets;
    }

    public void setPets(List<String> pets) {
        this.pets = pets;
    }

    public String getParticleArrowActive() {
        return particleArrowActive;
    }

    public void setParticleArrowActive(String particleArrowActive) {
        this.particleArrowActive = particleArrowActive;
    }

    public List<String> getParticleArrowAvailable() {
        return particleArrowAvailable;
    }

    public void setParticleArrowAvailable(List<String> particleArrowAvailable) {
        this.particleArrowAvailable = particleArrowAvailable;
    }

    public String getParticleDeathActive() {
        return particleDeathActive;
    }

    public void setParticleDeathActive(String particleDeathActive) {
        this.particleDeathActive = particleDeathActive;
    }

    public List<String> getParticleDeathAvailable() {
        return particleDeathAvailable;
    }

    public void setParticleDeathAvailable(List<String> particleDeathAvailable) {
        this.particleDeathAvailable = particleDeathAvailable;
    }

    public List<String> getEmotesAvailable() {
        return emotesAvailable;
    }

    public void setEmotesAvailable(List<String> emotesAvailable) {
        this.emotesAvailable = emotesAvailable;
    }

    public boolean getAllowsRidingPlayers() {
        return allowsRidingPlayers;
    }

    public void setAllowsRidingPlayers(boolean allowsRidingPlayers) {
        this.allowsRidingPlayers = allowsRidingPlayers;
    }

    public int getGems() {
        return gems;
    }

    public void setGems(int gems) {
        this.gems = gems;
    }

    public boolean getAllowsEmotes() {
        return allowsEmotes;
    }

    public void setAllowsEmotes(boolean allowsEmotes) {
        this.allowsEmotes = allowsEmotes;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public UUID getPlayerLink() {
        return playerLink;
    }
}
