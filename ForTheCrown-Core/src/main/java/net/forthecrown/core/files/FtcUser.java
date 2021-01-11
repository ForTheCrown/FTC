package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.enums.Ranks;
import net.forthecrown.core.enums.SellAmount;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FtcUser extends FtcFileManager {

    private final UUID base;
    public static Set<FtcUser> loadedData = new HashSet<>();

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
    private boolean allowsRidingPlayers;
    private int gems;

    //Added by Botul
    private boolean allowsEmotes;
    private Map<Material, Integer> itemPrices = new HashMap<>();
    private Map<Material, Integer> amountEarned = new HashMap<>();
    private SellAmount sellAmount;

    public FtcUser(UUID base){
        super(base.toString(), "playerdata");
        this.base = base;
        loadedData.add(this);

        if(fileDoesntExist) addDefaults();
        else reload();
        if(legacyDataExists()) convertLegacy();
    }

    public void reload(){
        super.reload();

        playerName = Bukkit.getOfflinePlayer(base).getName();
        knightRanks = getFile().getStringList("KnightRanks");
        currentRank = getFile().getString("CurrentRank");
        canSwapBranch = getFile().getBoolean("CanSwapBranch");
        pets = getFile().getStringList("Pets");
        particleArrowActive = getFile().getString("ParticleArrowActive");
        particleArrowAvailable = getFile().getStringList("ParticleArrowAvailable");
        particleDeathActive = getFile().getString("ParticleDeathActive");
        particleDeathAvailable = getFile().getStringList("ParticleDeathAvailable");
        allowsRidingPlayers = getFile().getBoolean("AllowsRidingPlayers");
        gems = getFile().getInt("Gems");
        allowsEmotes = getFile().getBoolean("AllowsEmotes");
        sellAmount = SellAmount.valueOf(getFile().getString("SellAmount"));

        if(getFile().getConfigurationSection("AmountEarned") != null){
            ConfigurationSection amountEarnedSec = getFile().getConfigurationSection("AmountEarned");
            Map<Material, Integer> tempMap = new HashMap<>();

            for(String s : amountEarnedSec.getKeys(true)){
                Material mat;
                try {
                    mat = Material.valueOf(s);
                } catch (NullPointerException e){
                    continue;
                }

                tempMap.put(mat, amountEarnedSec.getInt(s));
            }

            amountEarned = tempMap;
        }
        if(getFile().getConfigurationSection("ItemPrices") != null){
            ConfigurationSection itemPricesSec = getFile().getConfigurationSection("ItemPrices");
            Map<Material, Integer> tempMap = new HashMap<>();

            for(String s : itemPricesSec.getKeys(true)){
                Material mat;
                try {
                    mat = Material.valueOf(s);
                } catch (NullPointerException e){
                    continue;
                }

                tempMap.put(mat, itemPricesSec.getInt(s));
            }

            itemPrices = tempMap;
        }
    }
    public void save(){
        getFile().set("KnightRanks", getKnightRanks());
        getFile().set("PirateRanks", getPirateRanks());
        getFile().set("CurrentRank", getCurrentRank());
        getFile().set("CanSwapBranch", getCanSwapBranch());
        getFile().set("ActiveBranch", getActiveBranch());
        getFile().set("Pets", getPets());
        getFile().set("ParticleArrowActive", getParticleArrowActive());
        getFile().set("ParticleArrowAvailable", getParticleArrowAvailable());
        getFile().set("ParticleDeathActive", getParticleDeathActive());
        getFile().set("ParticleDeathAvailable", getParticleDeathAvailable());
        getFile().set("AllowsRidingPlayers", getAllowsRidingPlayers());
        getFile().set("Gems", getGems());
        getFile().set("AllowsEmotes", getAllowsEmotes());
        getFile().set("SellAmount", sellAmount.toString());

        if(getAmountEarnedMap().size() > 0){
            Map<String, Integer> tempMap = new HashMap<>();
            for (Material mat : getAmountEarnedMap().keySet()){
                tempMap.put(mat.toString(), getAmountEarnedMap().get(mat));
            }

            getFile().createSection("AmonutEarned", tempMap);
        }

        if(getItemPrices().size() > 0){
            Map<String, Integer> tempMap = new HashMap<>();
            for (Material mat : getItemPrices().keySet()){
                tempMap.put(mat.toString(), getItemPrices().get(mat));
            }

            getFile().createSection("ItemPrices", tempMap);
        }

        super.save();
    }
    public void unload(){
        save();
        loadedData.remove(this);
    }

    public int configurePriceForItem(Material item){
        int startPrice = Economy.getInstance().getItemPrice(item);
        int amountEarned1 = getAmountEarned(item);

        if(amountEarned1 <= 0) return startPrice;

        return (int) Math.ceil( (1+startPrice)*Math.exp( -amountEarned1*Math.log(1+startPrice)/500000 )-1 );
    }

    public UUID getBase(){
        return base;
    }

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

    public Integer getItemPrice(Material item){
        if(itemPrices.containsKey(item)) return itemPrices.get(item);
        return Economy.getInstance().getItemPrice(item);
    }
    public void setItemPrice(Material item, int price){
        itemPrices.put(item, price);
    }

    public Map<Material, Integer> getItemPrices() {
        return itemPrices;
    }
    public void setItemPrices(Map<Material, Integer> itemPrices) {
        this.itemPrices = itemPrices;
    }

    public Integer getAmountEarned(Material material){
        if(amountEarned.containsKey(material)) return amountEarned.get(material);
        return 0;
    }
    public void setAmountEarned(Material material, Integer amount){
        amountEarned.put(material, amount);

        setItemPrice(material, configurePriceForItem(material));
    }

    public Map<Material, Integer> getAmountEarnedMap() {
        return amountEarned;
    }
    public void setAmountEarnedMap(Map<Material, Integer> amountSold) {
        this.amountEarned = amountSold;
    }

    public boolean isBaron() {
        try {
            return FtcCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("baron").getScore(Bukkit.getOfflinePlayer(base).getName()).getScore() == 1;
        } catch (NullPointerException e){
            return false;
        }
    }
    public void setBaron(boolean baron) {
        int yayNay = 0;
        if(baron) yayNay = 1;
        FtcCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("baron").getScore(Bukkit.getPlayer(base).getName()).setScore(yayNay);
    }

    public SellAmount getSellAmount() {
        return sellAmount;
    }
    public void setSellAmount(SellAmount sellAmount) {
        this.sellAmount = sellAmount;
    }











    private void addDefaults(){
        getFile().addDefault("PlayerName", Bukkit.getPlayer(base).getName());
        getFile().addDefault("Ranks", new ArrayList<>());
        getFile().addDefault("CurrentRank", Ranks.DEFAULT.toString());
        getFile().addDefault("CanSwapBranch", true);
        getFile().addDefault("ActiveBranch", "Knight");
        getFile().addDefault("Pets", new ArrayList<>());
        getFile().addDefault("ParticleArrowActive", "none");
        getFile().addDefault("ParticleArrowAvailable", new ArrayList<>());
        getFile().addDefault("ParticleDeathActive", "none");
        getFile().addDefault("ParticleDeathAvailable", new ArrayList<>());
        getFile().addDefault("AllowsRidingPlayers", true);
        getFile().addDefault("Gems", 0);
        getFile().addDefault("AllowsEmotes", true);
        getFile().addDefault("SellAmount", SellAmount.PER_1.toString());
        getFile().options().copyDefaults(true);

        super.save();
        reload();
    }
    private void convertLegacy(){
        File oldFile = new File("plugins/DataPlugin/config.yml");
        FileConfiguration oldFileConfig = YamlConfiguration.loadConfiguration(oldFile);
        ConfigurationSection oldData = oldFileConfig.getConfigurationSection("players." + base.toString());
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
        setAllowsRidingPlayers(oldData.getBoolean("AllowsRidingPlayers"));
        setGems(oldData.getInt("Gems"));

        //changes the legacy section
        oldFileConfig.set("player." + base.toString(), "legacy");
        try {
            oldFileConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        save();
    }

    private boolean legacyDataExists(){
        File oldFile = new File("plugins/DataPlugin/config.yml");
        if(!oldFile.exists()) return false;
        ConfigurationSection oldData;
        try {
            oldData = YamlConfiguration.loadConfiguration(oldFile).getConfigurationSection("players." + base.toString());
        } catch (NullPointerException e){
            return false;
        }

        try {
            oldData.get("legacy");
        } catch (NullPointerException e1){
            return true;
        }

        return !oldData.contains("legacy");
    }
}