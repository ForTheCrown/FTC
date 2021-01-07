package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class FtcUserData extends FtcFileManager {

    private final UUID base;
    public static Set<FtcUserData> loadedData = new HashSet<>();

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
    private int balance; //doesn't get saved to the userdata itself, rather is saved in the Balances class
    private Map<Material, Integer> itemPrices;
    private Map<Material, Integer> amountSold;
    private boolean baron = false;

    public FtcUserData(UUID base){
        super(base.toString(), "playerdata");
        this.base = base;
        loadedData.add(this);

        if(needsDefaults) addDefaults();
        if(legacyDataExists()) convertLegacy();
        else reload();
    }

    public void reload(){
        super.reload();

        playerName = getFile().getString("PlayerName");
        knightRanks = getFile().getStringList("KnightRanks");
        pirateRanks = getFile().getStringList("PirateRanks");
        currentRank = getFile().getString("CurrentRank");
        canSwapBranch = getFile().getBoolean("CanSwapBranch");
        pets = getFile().getStringList("Pets");
        particleArrowActive = getFile().getString("ParticleArrowActive");
        particleArrowAvailable = getFile().getStringList("ParticleArrowAvailable");
        particleDeathActive = getFile().getString("ParticleDeathActive");
        particleDeathAvailable = getFile().getStringList("ParticleDeathAvailable");
        allowsRidingPlayers = getFile().getBoolean("AllowsRidingPlayers");
        gems = getFile().getInt("Gems");
        balance = getFile().getInt("Balance");
        allowsEmotes = getFile().getBoolean("AllowsEmotes");
        balance = Economy.getBalances().getBalance(base);

        baron = FtcCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("baron").getScore(Bukkit.getPlayer(base).getName()).getScore() == 1;

        for(String s : fileConfig.getConfigurationSection("ItemPrices").getKeys(true)){
            Material mat = Material.valueOf(s);
            Integer price = fileConfig.getConfigurationSection("ItemPrices").getInt(s);

            itemPrices.put(mat, price);
        }
        for(String s : fileConfig.getConfigurationSection("AmountSold").getKeys(true)){
            Material mat = Material.valueOf(s);
            Integer amount = fileConfig.getConfigurationSection("ItemPrices").getInt(s);

            amountSold.put(mat, amount);
        }
    }
    public void save(){
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
        fileConfig.set("AllowsRidingPlayers", getAllowsRidingPlayers());
        fileConfig.set("Gems", getGems());
        fileConfig.set("AllowsEmotes", getAllowsEmotes());
        fileConfig.set("Baron", baron);

        Economy.getBalances().setBalance(base, balance);
        Economy.getBalances().save();

        ConfigurationSection itemPriceSection = fileConfig.getConfigurationSection("ItemPrices");
        ConfigurationSection soldAmountItems = fileConfig.getConfigurationSection("AmountSold");
        for(Material mat : itemPrices.keySet()){
            itemPriceSection.set(mat.toString(), itemPrices.get(mat));
        }
        for(Material mat : amountSold.keySet()){
            soldAmountItems.set(mat.toString(), amountSold.get(mat));
        }

        fileConfig.set("ItemPrices", itemPriceSection);
        fileConfig.set("AmountSold", soldAmountItems);

        super.save();
    }
    public void unload(){
        save();
        loadedData.remove(this);
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

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
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


    public Integer getAmountSold(Material material){
        if(amountSold.containsKey(material)) return amountSold.get(material);
        return 0;
    }

    public void setAmountSold(Material material, Integer amount){
        amountSold.put(material, amount);
    }

    public Map<Material, Integer> getAmountSoldMap() {
        return amountSold;
    }

    public void setAmountSoldMap(Map<Material, Integer> amountSold) {
        this.amountSold = amountSold;
    }

    private void addDefaults(){
        fileConfig.addDefault("PlayerName", Bukkit.getPlayer(base).getName());
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
        fileConfig.addDefault("AllowsRidingPlayers", true);
        fileConfig.addDefault("Gems", 0);
        fileConfig.addDefault("AllowsEmotes", true);
        fileConfig.options().copyDefaults(true);

        super.save();
        reload();
    }
    private void convertLegacy(){
        ConfigurationSection oldData = FtcCore.getInstance().getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getConfigurationSection("Players." + base.toString());
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

        //deletes the legacy section
        FtcCore.getInstance().getConfig().set("players." + base.toString(), "Legacy");
        FtcCore.getInstance().saveConfig();
        save();
        reload();
    }

    private boolean legacyDataExists(){
        ConfigurationSection oldData = FtcCore.getInstance().getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getConfigurationSection("Players." + base.toString());
        if(oldData != null && !oldData.contains("legacy")) return true;
        return false;
    }
}