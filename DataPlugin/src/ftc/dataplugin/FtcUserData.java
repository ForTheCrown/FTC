package ftc.dataplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FtcUserData {

    private File file;
    private FileConfiguration fileConfig;

    private final DataPlugin main = DataPlugin.getInstance();

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
    private Map<Material, Integer> itemPrices;

    //Added by Botul
    private boolean allowsEmotes;
    private int balance;

    private final UUID playerLink;

    //constructors
    public FtcUserData(Player base){
        playerLink = base.getUniqueId();
        loadUserData();
    }
    public FtcUserData(UUID base){
        playerLink = base;
        loadUserData();
    }

    //file methods
    private void loadUserData(){
        main.loadedPlayerDatas.add(this);
        file = new File(DataPlugin.getInstance().getDataFolder() + "/playerdata", playerLink.toString() + ".yml");
        if(!file.exists()){
            if(main.getConfig().get("players." + playerLink.toString()) != "Legacy") legacyData = true;
            try {
                file.mkdir();
                file.createNewFile();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        fileConfig = YamlConfiguration.loadConfiguration(file);
        addDefaults();
        if(legacyData) legacyUpdate();
        reloadUserData();
    }
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
    public FileConfiguration getUserData(){
        return fileConfig;
    }
    public void saveUserData(){
        fileConfig.set("PlayerName", getPlayerName());
        fileConfig.set("KnightRanks", getKnightRanks());
        fileConfig.set("PirateRanks", getPirateRanks());
        fileConfig.set("CurrentRank", getCurrentRank());
        fileConfig.set("CanSwapBranch", getCanSwapBranch());
        fileConfig.set("ActiveBranch", getActiveBranch());
        fileConfig.set("Pets", getPets());
        fileConfig.set("ParticleArrowActive", getParticleArrowActive());
        fileConfig.set("ParticleArrowAvailable", getParticleArrowAvailable());
        fileConfig.set("ParticleDeathActive", getParticleDeathActive());
        fileConfig.set("ParticleDeathAvailable", getParticleDeathAvailable());
        fileConfig.set("EmotesAvailable", getEmotesAvailable());
        fileConfig.set("AllowsRidingPlayers", getAllowsRidingPlayers());
        fileConfig.set("Gems", getGems());
        fileConfig.set("Balance", getBalance());
        fileConfig.set("AllowsEmotes", getAllowsEmotes());

        try {
            fileConfig.save(file);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    //Takes the data from the config.yml and places it into the proper player data file
    private void legacyUpdate(){
        if (!legacyData) return;

        ConfigurationSection oldData = main.getConfig().getConfigurationSection("players." + playerLink.toString());
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

        //deletes the legacy section
        DataPlugin.getInstance().getConfig().set("players." + playerLink.toString(), "Legacy");
        saveUserData();
    }
    private void addDefaults(){
        fileConfig.addDefault("PlayerName", Bukkit.getPlayer(playerLink).getName());
        fileConfig.addDefault("KnightRanks", new ArrayList<>());
        fileConfig.addDefault("PirateRanks", new ArrayList<>());
        fileConfig.addDefault("CurrentRank", "default");
        fileConfig.addDefault("CanSwapBranch", true);
        fileConfig.addDefault("ActiveBranch", "Knight");
        fileConfig.addDefault("Pets", new ArrayList<>());
        fileConfig.addDefault("ParticleArrowActive", "none");
        fileConfig.addDefault("ParticleArrowAvailable", new ArrayList<>());
        fileConfig.addDefault("ParticleDeathActive", "none");
        fileConfig.addDefault("ParticleDeathAvailable", new ArrayList<>());
        fileConfig.addDefault("EmotesAvailable", new ArrayList<>());
        fileConfig.addDefault("AllowsRidingPlayers", true);
        fileConfig.addDefault("Gems", 0);
        fileConfig.addDefault("AllowsEmotes", true);
    }
    public void addDefaultPrices(){
        fileConfig.createSection("ItemPrices");
        fileConfig.getConfigurationSection("ItemPrices").createSection("Mob");
        ConfigurationSection mobPrices = fileConfig.getConfigurationSection("ItemPrices.Mob");
        mobPrices.addDefault("ROTTEN_FLESH", 2);
        mobPrices.addDefault("BONE", 5);
        mobPrices.addDefault("ARROW", 5);
        mobPrices.addDefault("STRING", 2);
        mobPrices.addDefault("SPIDER_EYE", 5);
        mobPrices.addDefault("LEATHER", 10);
        mobPrices.addDefault("GUNPOWDER", 5);
        mobPrices.addDefault("BLAZE_ROD", 5);
        mobPrices.addDefault("SLIME_BALL", 4);
        mobPrices.addDefault("COD", 3);
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
