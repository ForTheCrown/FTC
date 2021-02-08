package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
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

public class FtcUser extends FtcFileManager implements CrownUser {
    private final UUID base;

    //Already in the file, made by Wout
    private String name;
    private Rank currentRank;
    private Set<Rank> ranks;
    private Branch branch;
    private boolean canSwapBranch = true;
    private List<String> pets;
    private Particle particleArrowActive;
    private List<Particle> particleArrowAvailable;
    private String particleDeathActive;
    private List<String> particleDeathAvailable;
    private boolean allowsRidingPlayers = true;
    private int gems;

    //Added by Botul
    private boolean allowsEmotes = true;
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

        FtcCore.loadedUsers.add(this);
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
        setNextResetTime(getFile().getLong("TimeStamps.NextResetTime"));
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

        if(isOnline() && name != null && !name.equals(getPlayer().getName())){ //transfers all scores to new player name if player name changes
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
        getFile().set("TimeStamps.NextResetTime", getNextResetTime());
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
                if(!getItemPrice(mat).equals(FtcCore.getInstance().getItemPrice(mat))) tempMap.put(mat.toString(), getItemPrice(mat));
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

    @Override
    public void unload(){
        save();
        FtcCore.loadedUsers.remove(this);
    }

    @Override
    public int configurePriceForItem(Material item){
        int startPrice = FtcCore.getInstance().getItemPrice(item);
        int amountEarned1 = getAmountEarned(item);

        if(amountEarned1 <= 0) return startPrice;

        return (int) Math.ceil( (1+startPrice)*Math.exp( -amountEarned1*Math.log(1+startPrice)/500000 )-1 );
    }


    @Override
    public UUID getBase(){
        return base;
    }

    @Override
    public Player getPlayer(){
        return Bukkit.getPlayer(getBase());
    }

    @Override
    public OfflinePlayer getOfflinePlayer(){
        return Bukkit.getOfflinePlayerIfCached(getName());
    }

    @Override
    public Set<Rank> getAvailableRanks(){
        return ranks;
    }

    @Override
    public void setAvailableRanks(Set<Rank> ranks){
        this.ranks = ranks;
    }

    @Override
    public boolean hasRank(Rank rank){
        return getAvailableRanks().contains(rank);
    }

    @Override
    public void addRank(Rank rank){
        ranks.add(rank);
    }

    @Override
    public void removeRank(Rank rank){
        if(hasRank(rank)) ranks.remove(rank);
    }

    @Override
    public Rank getRank(){
        return currentRank;
    }
    @Override
    public void setRank(Rank rank){
        setRank(rank, true);
    }

    @Override
    public void setRank(Rank rank, boolean setPrefix){
        currentRank = rank;

        if(setPrefix) setTabPrefix(rank.getColorlessPrefix());
    }

    @Override
    public boolean getCanSwapBranch() {
        return canSwapBranch;
    }

    @Override
    public void setCanSwapBranch(boolean canSwapBranch) {
        this.canSwapBranch = canSwapBranch;
    }

    @Override
    public List<String> getPets() {
        return pets;
    }

    @Override
    public void setPets(List<String> pets) {
        this.pets = pets;
    }

    @Override
    public Particle getArrowParticle() {
        return particleArrowActive;
    }
    @Override
    public void setArrowParticle(Particle particleArrowActive) {
        this.particleArrowActive = particleArrowActive;
    }

    @Override
    public List<Particle> getParticleArrowAvailable() {
        return particleArrowAvailable;
    }
    @Override
    public void setParticleArrowAvailable(List<Particle> particleArrowAvailable) {
        this.particleArrowAvailable = particleArrowAvailable;
    }

    @Override
    public String getDeathParticle() {
        return particleDeathActive;
    }
    @Override
    public void setDeathParticle(String particleDeathActive) {
        this.particleDeathActive = particleDeathActive;
    }

    @Override
    public List<String> getParticleDeathAvailable() {
        return particleDeathAvailable;
    }
    @Override
    public void setParticleDeathAvailable(List<String> particleDeathAvailable) {
        this.particleDeathAvailable = particleDeathAvailable;
    }
    @Override
    public boolean allowsRidingPlayers() {
        return allowsRidingPlayers;
    }
    @Override
    public void setAllowsRidingPlayers(boolean allowsRidingPlayers) {
        this.allowsRidingPlayers = allowsRidingPlayers;
    }

    @Override
    public int getGems() {
        return gems;
    }
    @Override
    public void setGems(int gems) {
        this.gems = gems;
    }
    @Override
    public void addGems(int gems){
        this.gems += gems;
    }

    @Override
    public boolean allowsEmotes() {
        return allowsEmotes;
    }
    @Override
    public void setAllowsEmotes(boolean allowsEmotes) {
        this.allowsEmotes = allowsEmotes;
    }

    @Override
    public Integer getItemPrice(Material item){
        int i;
        try {
            i = itemPrices.get(item);
        } catch (NullPointerException e){
            return FtcCore.getInstance().getItemPrice(item);
        }
        return i;
    }
    @Override
    public void setItemPrice(Material item, int price){
        itemPrices.put(item, price);
    }

    @Override
    public Map<Material, Integer> getItemPrices() {
        return itemPrices;
    }
    @Override
    public void setItemPrices(@Nonnull Map<Material, Integer> itemPrices) {
        this.itemPrices = itemPrices;
    }

    @Override
    public Integer getAmountEarned(Material material){
        int i;
        try {
            i = getAmountEarnedMap().get(material);
        } catch (NullPointerException e){
            return 0;
        }
        return i;
    }
    @Override
    public void setAmountEarned(Material material, Integer amount){
        amountEarned.put(material, amount);

        setItemPrice(material, configurePriceForItem(material));
    }

    @Override
    public Map<Material, Integer> getAmountEarnedMap() {
        return amountEarned;
    }
    @Override
    public void setAmountEarnedMap(@Nonnull Map<Material, Integer> amountSold) {
        this.amountEarned = amountSold;
    }

    @Override
    public boolean isBaron() {
        Score score = FtcCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("Baron").getScore(getName());
        return score.isScoreSet() && score.getScore() == 1;
    }
    @Override
    public void setBaron(boolean baron) {
        int yayNay = baron ? 1 : 0;
        FtcCore.getInstance().getServer().getScoreboardManager().getMainScoreboard().getObjective("Baron").getScore(getName()).setScore(yayNay);
        if(baron){
            addRank(Rank.BARON);
            addRank(Rank.BARONESS);
        }
        else{
             removeRank(Rank.BARON);
             removeRank(Rank.BARONESS);
        }
    }

    @Override
    public SellAmount getSellAmount() {
        return sellAmount;
    }
    @Override
    public void setSellAmount(SellAmount sellAmount) {
        this.sellAmount = sellAmount;
    }

    @Override
    public long getTotalEarnings(){
        return totalEarnings;
    }

    @Override
    public void setTotalEarnings(long amount){
        totalEarnings = amount;
    }
    @Override
    public void addTotalEarnings(long amount){
        setTotalEarnings(getTotalEarnings() + amount);
    }

    @Override
    public long getNextResetTime(){
        return nextResetTime;
    }
    @Override
    public void setNextResetTime(long nextResetTime) {
        this.nextResetTime = nextResetTime;
    }

    @Override
    public void resetEarnings(){
        amountEarned.clear();
        itemPrices.clear();
        setTotalEarnings(0);

        save();
        nextResetTime = System.currentTimeMillis() + FtcCore.getUserDataResetInterval();
    }

    @Override
    public String getName(){
        if(name != null) return name;
        else if(Bukkit.getPlayer(getBase()) != null) return Bukkit.getPlayer(getBase()).getName();
        else return Bukkit.getOfflinePlayer(getBase()).getName();
    }

    @Override
    public Branch getBranch(){
        return branch;
    }

    @Override
    public void setBranch(Branch branch){
        this.branch = branch;
    }

    @Override
    public void setTabPrefix(String s){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + getName() + " tabprefix " + s);
    }

    @Override
    public void clearTabPrefix(){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + getName() + " tabprefix ");
    }

    @Override
    public void sendMessage(String message){
        if(!isOnline()) return;
        getPlayer().sendMessage(FtcCore.translateHexCodes(message));
    }

    @Override
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

    @Override
    public String toString() {
        return "FtcUser{" +
                "base=" + base +
                ", name='" + name + '\'' +
                ", currentRank=" + currentRank +
                ", branch=" + branch +
                ", canSwapBranch=" + canSwapBranch +
                ", gems=" + gems +
                ", allowsEmotes=" + allowsEmotes +
                ", totalEarnings=" + totalEarnings +
                ", nextResetTime=" + nextResetTime +
                '}';
    }

    private void permsCheck(){
        if(!isOnline()) return;
        if(getName().contains("Paranor")) addRank(Rank.LEGEND);

        if(isBaron() && !hasRank(Rank.BARON)){
            addRank(Rank.BARON);
            addRank(Rank.BARONESS);
        }

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
        getFile().addDefault("TimeStamps.NextResetTime", System.currentTimeMillis() + FtcCore.getUserDataResetInterval());
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

        setRank(Rank.valueOf(oldYaml.getString("CurrentRank").toUpperCase()), false);
        setCanSwapBranch(oldYaml.getBoolean("CanSwapBranch"));
        setPets(oldYaml.getStringList("Pets"));
        setAllowsRidingPlayers(oldYaml.getBoolean("AllowsRidingPlayers"));
        setDeathParticle(oldYaml.getString("ParticleDeathActive"));
        setParticleDeathAvailable(oldYaml.getStringList("ParticleDeathAvailable"));
        setGems(oldYaml.getInt("Gems"));

        if(oldYaml.get("ParticleArrowActive") != null && !oldYaml.getString("ParticleArrowActive").contains("none")) setArrowParticle(Particle.valueOf(oldYaml.getString("ParticleArrowActive")));

        //branch conversion
        if(oldYaml.getString("ActiveBranch").contains("Knight")) setBranch(Branch.ROYALS);
        else setBranch(Branch.valueOf(oldYaml.getString("ActiveBranch").toUpperCase() + "S"));

        //Rank conversion
        Set<Rank> tempList = new HashSet<>();
        if(oldYaml.getList("PirateRanks") != null && oldYaml.getList("PirateRanks").size() > 0){
            for (String s : oldYaml.getStringList("PirateRanks")){
                try { tempList.add(Rank.valueOf(s.toUpperCase() + "S"));
                } catch (Exception ignored){}
            }
        }
        if(oldYaml.getList("KnightRanks") != null && oldYaml.getList("KnightRanks").size() > 0){
            for (String s : oldYaml.getStringList("KnightRanks")){
                if(s.toLowerCase().contains("baron")) addRank(Rank.BARONESS);

                try { tempList.add(Rank.valueOf(s.toUpperCase()));
                } catch (Exception ignored){}
            }
        }
        tempList.add(Rank.DEFAULT);
        setAvailableRanks(tempList);
        setBranch(getRank().getRankBranch());

        if(oldYaml.getList("ParticleArrowAvailable") != null){
            List<Particle> tempList1 = new ArrayList<>();
            for (String s : oldYaml.getStringList("ParticleArrowAvailable")){
                try { tempList1.add(Particle.valueOf(s.toUpperCase()));
                } catch (Exception ignored){}
            }
            setParticleArrowAvailable(tempList1);
        }

        if(oldYaml.getStringList("EmotesAvailable").size() > 0){
            for (String s : oldYaml.getStringList("EmotesAvailable")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + getName() + " permission set ftc.emotes." + s.toLowerCase());
            }
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