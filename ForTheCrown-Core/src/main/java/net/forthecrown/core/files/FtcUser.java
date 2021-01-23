package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FtcUser extends FtcFileManager {

    public static final Set<FtcUser> loadedData = new HashSet<>();

    private final UUID base;
    private final Balances balance = FtcCore.getBalances();

    //Already in the file, added by Wout
    private String name;
    private Rank currentRank;
    private Set<Rank> ranks;
    private Branch branch;
    private boolean canSwapBranch;
    private List<String> pets;
    private Particle particleArrowActive;
    private List<Particle> particleArrowAvailable;
    private String particleDeathActive;
    private List<String> particleDeathAvailable;
    private boolean allowsRidingPlayers;
    private int gems;

    //Added by Botul
    private boolean allowsEmotes;
    private Map<Material, Integer> itemPrices = new HashMap<>();
    private Map<Material, Integer> amountEarned = new HashMap<>();
    private SellAmount sellAmount;
    private long totalEarnings;
    private long nextResetTime;


    public FtcUser(UUID base){
        super(base.toString(), "playerdata");
        this.base = base;

        if(fileDoesntExist) addDefaults();
        else reload();
        if(legacyDataExists()) convertLegacy();
        if(shouldResetEarnings()) resetEarnings();

        loadedData.add(this);
        permsCheck();
    }

    @Override
    public void reload(){
        super.reload();

        name = getFile().getString("PlayerName");
        setRank(Rank.valueOf(getFile().getString("CurrentRank")), false);
        setBranch(Branch.valueOf(getFile().getString("Branch")));
        setCanSwapBranch(getFile().getBoolean("CanSwapBranch"));
        setPets(getFile().getStringList("Pets"));
        setDeathParticle(getFile().getString("DeathParticle"));
        setParticleDeathAvailable(getFile().getStringList("ParticleDeathAvailable"));
        setAllowsRidingPlayers(getFile().getBoolean("AllowsRidingPlayers"));
        setGems(getFile().getInt("Gems"));
        setAllowsEmotes(getFile().getBoolean("AllowsEmotes"));
        setTotalEarnings(getFile().getLong("TotalEarnings"));
        setNextResetTime(getFile().getLong("NextResetTime"));
        setSellAmount(SellAmount.valueOf(getFile().getString("SellAmount").toUpperCase()));

        if(!getFile().getString("ArrowParticle").contains("none")) setArrowParticle(Particle.valueOf(getFile().getString("ArrowParticle")));
        else setArrowParticle(null);

        if(getFile().getList("AvailableRanks") != null){
            Set<Rank> tempList = new HashSet<>();
            for (String s : getFile().getStringList("AvailableRanks")){
                tempList.add(Rank.valueOf(s));
            }
            setAvailableRanks(tempList);
        } else setAvailableRanks(new HashSet<>());

        if(getFile().getList("ArrowParticleAvailable") != null){
            List<Particle> tempList = new ArrayList<>();
            for(String s : getFile().getStringList("ArrowParticleAvailable")){
                tempList.add(Particle.valueOf(s));
            }
            setParticleArrowAvailable(tempList);
        } else setParticleDeathAvailable(new ArrayList<>());

        if(getFile().getConfigurationSection("ItemPrices") != null && getFile().getConfigurationSection("ItemPrices").getKeys(true).size() > 0){
            Map<Material, Integer> tempMap = new HashMap<>();
            for (String s : getFile().getConfigurationSection("ItemPrices").getKeys(false)){
                tempMap.put(Material.valueOf(s), getFile().getConfigurationSection("ItemPrices").getInt(s));
            }
            setItemPrices(tempMap);
        }

        if(getFile().getConfigurationSection("AmountEarned") != null && getFile().getConfigurationSection("AmountEarned").getKeys(true).size() > 0){
            Map<Material, Integer> tempMap = new HashMap<>();
            for (String s : getFile().getConfigurationSection("AmountEarned").getKeys(false)){
                tempMap.put(Material.valueOf(s), getFile().getConfigurationSection("AmountEarned").getInt(s));
            }
            setAmountEarnedMap(tempMap);
        }

        if(isOnline() && !name.equals(getPlayer().getName())){ //transfers all scores to new player name if player name changes
            Scoreboard scoreboard = getPlayer().getScoreboard();
            for (Objective obj : scoreboard.getObjectives()){
                if(!obj.getScore(name).isScoreSet()) continue;

                obj.getScore(getPlayer().getName()).setScore(obj.getScore(name).getScore());
                obj.getScore(name).setScore(0);
            }
            name = getPlayer().getName();
        }
    }

    @Override
    public void save(){
        getFile().set("PlayerName", getName());
        getFile().set("CurrentRank", getRank().toString());
        getFile().set("Branch", getBranch().toString());
        getFile().set("CanSwapBranch", getCanSwapBranch());
        getFile().set("Pets", getPets());
        getFile().set("DeathParticle", getDeathParticle());
        getFile().set("ParticleDeathAvailable", getParticleDeathAvailable());
        getFile().set("AllowsRidingPlayers", allowsRidingPlayers());
        getFile().set("Gems", getGems());
        getFile().set("SellAmount", getSellAmount().toString());
        getFile().set("TotalEarnings", getTotalEarnings());
        getFile().set("NextResetTime", getNextResetTime());
        getFile().set("AllowsEmotes", allowsEmotes());

        if(getArrowParticle() == null) getFile().set("ArrowParticle", "none");
        else getFile().set("ArrowParticle", getArrowParticle().toString());

        //rank saving
        if(getAvailableRanks().size() > 0){
            List<String> tempList = new ArrayList<>();
            for(Rank r : getAvailableRanks()){
                tempList.add(r.toString());
            }
            getFile().set("AvailableRanks", tempList);
        }

        //Arrow particle saving
        if(getParticleArrowAvailable().size() > 0){
            List<String> tempList = new ArrayList<>();
            for (Particle p : getParticleArrowAvailable()){
                tempList.add(p.toString());
            }
            getFile().set("ArrowParticleAvailable", tempList);
        }

        //Item price saving
        if(getItemPrices().size() > 0){
            Map<String, Integer> tempMap = new HashMap<>();

            for (Material mat : getItemPrices().keySet()){
                tempMap.put(mat.toString(), getItemPrice(mat));
            }
            getFile().createSection("ItemPrices", tempMap);
        }

        //Amount earned saving
        if(getAmountEarnedMap().size() > 0){
            Map<String, Integer> tempMap = new HashMap<>();

            for (Material mat : getAmountEarnedMap().keySet()){
                tempMap.put(mat.toString(), getAmountEarned(mat));
            }
            getFile().createSection("AmountEarned", tempMap);
        }

        super.save();
    }

    public void unload(){
        save();
        loadedData.remove(this);
    }

    public int configurePriceForItem(Material item){
        int startPrice = FtcCore.getInstance().getItemPrice(item);
        int amountEarned1 = getAmountEarned(item);

        if(amountEarned1 <= 0) return startPrice;

        return (int) Math.ceil( (1+startPrice)*Math.exp( -amountEarned1*Math.log(1+startPrice)/500000 )-1 );
    }


    public UUID getBase(){
        return base;
    }

    public Player getPlayer(){
        return Bukkit.getPlayer(getBase());
    }

    public OfflinePlayer getOfflinePlayer(){
        return Bukkit.getOfflinePlayerIfCached(getName());
    }

    public Set<Rank> getAvailableRanks(){
        return ranks;
    }
    public void setAvailableRanks(Set<Rank> ranks){
        this.ranks = ranks;
    }

    public boolean hasRank(Rank rank){
        return getAvailableRanks().contains(rank);
    }

    public void addRank(Rank rank){
        ranks.add(rank);
    }
    public void removeRank(Rank rank){
        if(hasRank(rank)) ranks.remove(rank);
    }

    public Rank getRank(){
        return currentRank;
    }
    public void setRank(Rank rank){
        setRank(rank, true);
    }

    public void setRank(Rank rank, boolean setPrefix){
        currentRank = rank;

        if(setPrefix) setTabPrefix(rank.getColorlessPrefix());
    }

    public boolean getCanSwapBranch() {
        return canSwapBranch;
    }
    public void setCanSwapBranch(boolean canSwapBranch) {
        this.canSwapBranch = canSwapBranch;
    }

    public List<String> getPets() {
        return pets;
    }
    public void setPets(List<String> pets) {
        this.pets = pets;
    }

    public Particle getArrowParticle() {
        return particleArrowActive;
    }
    public void setArrowParticle(Particle particleArrowActive) {
        this.particleArrowActive = particleArrowActive;
    }

    public List<Particle> getParticleArrowAvailable() {
        return particleArrowAvailable;
    }
    public void setParticleArrowAvailable(List<Particle> particleArrowAvailable) {
        this.particleArrowAvailable = particleArrowAvailable;
    }

    public String getDeathParticle() {
        return particleDeathActive;
    }
    public void setDeathParticle(String particleDeathActive) {
        this.particleDeathActive = particleDeathActive;
    }

    public List<String> getParticleDeathAvailable() {
        return particleDeathAvailable;
    }
    public void setParticleDeathAvailable(List<String> particleDeathAvailable) {
        this.particleDeathAvailable = particleDeathAvailable;
    }
    public boolean allowsRidingPlayers() {
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
    public void addGems(int gems){
        this.gems += gems;
    }

    public boolean allowsEmotes() {
        return allowsEmotes;
    }
    public void setAllowsEmotes(boolean allowsEmotes) {
        this.allowsEmotes = allowsEmotes;
    }

    public Integer getItemPrice(Material item){
        int i;
        try {
            i = itemPrices.get(item);
        } catch (NullPointerException e){
            return FtcCore.getInstance().getItemPrice(item);
        }
        return i;
    }
    public void setItemPrice(Material item, int price){
        itemPrices.put(item, price);
    }

    public Map<Material, Integer> getItemPrices() {
        return itemPrices;
    }
    public void setItemPrices(@Nonnull Map<Material, Integer> itemPrices) {
        this.itemPrices = itemPrices;
    }

    public Integer getAmountEarned(Material material){
        int i;
        try {
            i = getAmountEarnedMap().get(material);
        } catch (NullPointerException e){
            return 0;
        }
        return i;
    }
    public void setAmountEarned(Material material, Integer amount){
        amountEarned.put(material, amount);

        setItemPrice(material, configurePriceForItem(material));
    }

    public Map<Material, Integer> getAmountEarnedMap() {
        return amountEarned;
    }
    public void setAmountEarnedMap(@Nonnull Map<Material, Integer> amountSold) {
        this.amountEarned = amountSold;
    }

    public boolean isBaron() {
        Score score = getPlayer().getScoreboard().getObjective("baron").getScore(getName());
        return score.isScoreSet() && score.getScore() == 1;
    }
    public void setBaron(boolean baron) {
        int yayNay = baron ? 1 : 0;
        FtcCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("baron").getScore(getName()).setScore(yayNay);
    }

    public SellAmount getSellAmount() {
        return sellAmount;
    }
    public void setSellAmount(SellAmount sellAmount) {
        this.sellAmount = sellAmount;
    }

    public long getTotalEarnings(){
        return totalEarnings;
    }

    public void setTotalEarnings(long amount){
        totalEarnings = amount;
    }
    public void addTotalEarnings(long amount){
        setTotalEarnings(getTotalEarnings() + amount);
    }

    public long getNextResetTime(){
        return nextResetTime;
    }
    public void setNextResetTime(long nextResetTime) {
        this.nextResetTime = nextResetTime;
    }

    public void resetEarnings(){
        amountEarned.clear();
        itemPrices.clear();
        setTotalEarnings(0);

        save();
        nextResetTime = System.currentTimeMillis() + FtcCore.getUserDataResetInterval();
    }

    public String getName(){
        return name;
    }

    public Branch getBranch(){
        return branch;
    }

    public void setBranch(Branch branch){
        this.branch = branch;
    }

    public void setTabPrefix(String s){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + getName() + " tabprefix " + s);
    }

    public void clearTabPrefix(){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + getName() + " tabprefix ");
    }

    public void sendMessage(String message){
        if(!isOnline()) return;
        getPlayer().sendMessage(FtcCore.translateHexCodes(message));
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FtcUser user = (FtcUser) o;
        return getCanSwapBranch() == user.getCanSwapBranch() &&
                allowsRidingPlayers() == user.allowsRidingPlayers() &&
                getGems() == user.getGems() &&
                allowsEmotes() == user.allowsEmotes() &&
                getTotalEarnings() == user.getTotalEarnings() &&
                getBase().equals(user.getBase()) &&
                getRank() == user.getRank() &&
                getPets().equals(user.getPets()) &&
                getSellAmount() == user.getSellAmount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBase(), currentRank, getCanSwapBranch(), getPets(), particleArrowActive, particleDeathActive, allowsRidingPlayers, getGems(), allowsEmotes, getSellAmount(), getTotalEarnings());
    }


    private void permsCheck(){
        if(!isOnline()) return;
        if(getPlayer().hasPermission("ftc.donator3")){
            addRank(Rank.PRINCE);
            addRank(Rank.PRINCESS);
            addRank(Rank.ADMIRAL);
        }

        if(getPlayer().hasPermission("ftc.donator2")){
            addRank(Rank.DUKE);
            addRank(Rank.DUCHESS);
            addRank(Rank.CAPTAIN);
        }

        if(getPlayer().hasPermission("ftc.donator1") || getPlayer().hasPermission("ftc.donator")){
            addRank(Rank.LORD);
            addRank(Rank.LADY);
        }
    }

    private boolean shouldResetEarnings(){
        return nextResetTime - System.currentTimeMillis() >= 0;
    }

    private void addDefaults(){
        getFile().addDefault("PlayerName", getName());
        getFile().addDefault("CurrentRank", Rank.DEFAULT.toString());
        getFile().addDefault("AvailableRanks", new ArrayList<>(Collections.singleton(Rank.DEFAULT.toString())));
        getFile().addDefault("CanSwapBranch", true);
        getFile().addDefault("Branch", Branch.DEFAULT.toString());
        getFile().addDefault("Pets", new ArrayList<>());
        getFile().addDefault("ArrowParticle", "none");
        getFile().addDefault("ArrowParticleAvailable", new ArrayList<>());
        getFile().addDefault("DeathParticle", "none");
        getFile().addDefault("DeathParticleAvailable", new ArrayList<>());
        getFile().addDefault("AllowsRidingPlayers", true);
        getFile().addDefault("Gems", 0);
        getFile().addDefault("AllowsEmotes", true);
        getFile().addDefault("SellAmount", SellAmount.PER_1.toString());
        getFile().addDefault("TotalEarnings", 0);
        getFile().addDefault("NextResetTime", System.currentTimeMillis() + FtcCore.getUserDataResetInterval());
        getFile().options().copyDefaults(true);

        super.save();
        reload();
    }

    private boolean legacyDataExists(){
        File oldFile = new File("plugins/DataPlugin/config.yml");
        if(!oldFile.exists()) return false;
        FileConfiguration oldYaml = YamlConfiguration.loadConfiguration(oldFile);
        return (oldYaml.get("players." + base.toString()) != null);
    }

    private void convertLegacy(){
        File oldFile = new File("plugins/DataPlugin/config.yml");
        FileConfiguration oldYamlFile = YamlConfiguration.loadConfiguration(oldFile);
        ConfigurationSection oldYaml = oldYamlFile.getConfigurationSection("players." + getBase().toString());

        setRank(Rank.valueOf(oldYaml.getString("CurrentRank").toUpperCase()));
        setCanSwapBranch(oldYaml.getBoolean("CanSwapBranch"));
        setPets(oldYaml.getStringList("Pets"));
        setAllowsRidingPlayers(oldYaml.getBoolean("AllowsRidingPlayers"));
        setDeathParticle(oldYaml.getString("ParticleDeathActive"));
        setParticleDeathAvailable(oldYaml.getStringList("ParticleDeathAvailable"));
        setGems(oldYaml.getInt("Gems"));

        if(oldYaml.get("ParticleArrowActive") != null && !oldYaml.getString("ParticleArrowActive").contains("none")) setArrowParticle(Particle.valueOf(oldYaml.getString("ParticleArrowActive")));

        //Rank conversion
        Set<Rank> tempList = new HashSet<>();
        if(oldYaml.getList("PirateRanks") != null && oldYaml.getList("PirateRanks").size() > 0){
            for (String s : oldYaml.getStringList("PirateRanks")){
                try { tempList.add(Rank.valueOf(s.toUpperCase()));
                } catch (Exception ignored){}
            }
        }
        if(oldYaml.getList("KnightRanks") != null && oldYaml.getList("KnightRanks").size() > 0){
            for (String s : oldYaml.getStringList("KnightRanks")){
                try { tempList.add(Rank.valueOf(s.toUpperCase()));
                } catch (Exception ignored){}
            }
        }
        tempList.add(Rank.DEFAULT);
        setAvailableRanks(tempList);
        setBranch(getRank().getRankBranch());

        if(oldYaml.getList("ParticleArrowAvailable ") != null){
            List<Particle> tempList1 = new ArrayList<>();
            for (String s : oldYaml.getStringList("ParticleArrowAvailable ")){
                try { tempList1.add(Particle.valueOf(s.toUpperCase()));
                } catch (Exception ignored){}
            }
            setParticleArrowAvailable(tempList1);
        }

        oldYamlFile.set("players." + getBase().toString(), null);
        try {
            oldYamlFile.save(oldFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        addRank(Rank.DEFAULT);

        permsCheck();
        save();
    }
}