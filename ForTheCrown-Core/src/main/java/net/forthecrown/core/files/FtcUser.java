package net.forthecrown.core.files;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.exceptions.UserNotOnlineException;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class FtcUser extends FtcFileManager implements CrownUser {

    private final UUID base;
    private String name;

    private Rank currentRank;
    private Set<Rank> ranks;
    private Branch branch;

    private List<String> pets;
    private Particle particleArrowActive;
    private List<Particle> particleArrowAvailable;
    private String particleDeathActive;
    private List<String> particleDeathAvailable;

    private int gems;
    private boolean canSwapBranch = true;
    private boolean allowsEmotes = true;
    private boolean allowsRidingPlayers = true;
    private long totalEarnings;
    private long nextResetTime;
    private long nextBranchSwapAllowed;

    private Map<Material, Integer> itemPrices = new HashMap<>();
    private Map<Material, Integer> amountEarned = new HashMap<>();
    private SellAmount sellAmount;

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
        setCanSwapBranch(getFile().getBoolean("CanSwapBranch"), false);
        setPets(getFile().getStringList("Pets"));
        setDeathParticle(getFile().getString("DeathParticle"));
        setParticleDeathAvailable(getFile().getStringList("ParticleDeathAvailable"));
        setAllowsRidingPlayers(getFile().getBoolean("AllowsRidingPlayers"));
        setGems(getFile().getInt("Gems"));
        setAllowsEmotes(getFile().getBoolean("AllowsEmotes"));
        setTotalEarnings(getFile().getLong("TotalEarnings"));
        setSellAmount(SellAmount.valueOf(getFile().getString("SellAmount").toUpperCase()));

        setNextResetTime(getFile().getLong("TimeStamps.NextResetTime"));

        nextBranchSwapAllowed = getFile().getLong("TimeStamps.NextBranchSwap");
        if(nextBranchSwapAllowed != 0) checkBranchSwapping();

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

        itemPrices.clear();
        if(getFile().getConfigurationSection("ItemPrices") != null && getFile().getConfigurationSection("ItemPrices").getKeys(true).size() > 0){
            Map<Material, Integer> tempMap = new HashMap<>();
            for (String s : getFile().getConfigurationSection("ItemPrices").getKeys(false)){
                tempMap.put(Material.valueOf(s), getFile().getConfigurationSection("ItemPrices").getInt(s));
            }
            setItemPrices(tempMap);
        }

        amountEarned.clear();
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
        if(deleted) return;

        getFile().set("PlayerName", getName());
        getFile().set("CurrentRank", getRank().toString());
        getFile().set("Branch", getBranch().toString());
        getFile().set("CanSwapBranch", canSwapBranch);
        getFile().set("Pets", getPets());
        getFile().set("DeathParticle", getDeathParticle());
        getFile().set("ParticleDeathAvailable", getParticleDeathAvailable());
        getFile().set("AllowsRidingPlayers", allowsRidingPlayers());
        getFile().set("Gems", getGems());
        getFile().set("SellAmount", getSellAmount().toString());
        getFile().set("TotalEarnings", getTotalEarnings());
        getFile().set("AllowsEmotes", allowsEmotes());

        getFile().set("TimeStamps.NextResetTime", getNextResetTime());
        getFile().set("TimeStamps.LastLoad", System.currentTimeMillis());
        if(!canSwapBranch) getFile().set("TimeStamps.NextBranchSwap", nextBranchSwapAllowed);
        else getFile().set("TimeStamps.NextBranchSwap", null);

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
                if(getItemPrice(mat) < FtcCore.getInstance().getItemPrice(mat)) tempMap.put(mat.toString(), getItemPrice(mat));
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
        return Bukkit.getOfflinePlayer(getBase());
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
        checkBranchSwapping();
        return canSwapBranch;
    }

    @Override
    public void setCanSwapBranch(boolean canSwapBranch, boolean addToCooldown) {
        this.canSwapBranch = canSwapBranch;

        if(addToCooldown) nextBranchSwapAllowed = System.currentTimeMillis() + FtcCore.getBranchSwapCooldown();
        else nextBranchSwapAllowed = 0;
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
            if(i > FtcCore.getInstance().getItemPrice(item)) i = FtcCore.getInstance().getItemPrice(item);
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
        System.out.println(getName() + " earnings reset, next reset in: " + ((((getNextResetTime()/1000)/60)/60)/24) + " days");
    }

    @Override
    public String getName(){
        if(name == null) name = getOfflinePlayer().getName();
        return name;
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
    public void setTabPrefix(@Nullable String s){
        if(s == null || s.isBlank()) clearTabPrefix();
        else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + getName() + " tabprefix " + s);
    }

    @Override
    public void clearTabPrefix(){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + getName() + " tabprefix ");
    }

    @Override
    public void sendMessage(@Nonnull String message){
        if(!isOnline()) return;
        getPlayer().sendMessage(CrownUtils.translateHexCodes(message));
    }

    @Override
    public void sendMessage(@Nonnull String... messages){
        for (String s: messages){
            sendMessage(s);
        }
    }

    @Override
    public void sendMessage(UUID sender, String message) {
        performOnlineCheck();
        getPlayer().sendMessage(sender, CrownUtils.translateHexCodes(message));
    }

    @Override
    public void sendMessage(UUID sender, String[] messages) {
        for (String s: messages){
            sendMessage(sender, s);
        }
    }

    @Override
    public Server getServer() {
        return FtcCore.getInstance().getServer();
    }

    @Override
    public boolean isOnline() {
        return getPlayer() != null;
    }

    @Override
    public boolean isKing() {
        return FtcCore.getKing().equals(getBase());
    }

    @Override
    public void setKing(boolean king, boolean setPrefix) {
        setKing(king, setPrefix, false);
    }

    @Override
    public Scoreboard getScoreboard(){
        return FtcCore.getInstance().getServer().getScoreboardManager().getMainScoreboard();
    }

    @Override
    public void setKing(boolean king, boolean setPrefix, boolean isFemale) {
        if(king){
            if(setPrefix){
                if(!isFemale) setTabPrefix("&l[&e&lKing&r&l] &r");
                else setTabPrefix("&l[&e&lQueen&r&l] &r");
            }
            FtcCore.setKing(getBase());
            return;
        }

        FtcCore.setKing(null);
        if(setPrefix) setTabPrefix(getRank().getColorlessPrefix());
    }

    @Override
    public void setKing(boolean king) {
        setKing(king, false, false);
    }

    @Override
    public void delete() {
        super.delete();
    }

    //---------------------------
    // The following methods are inherited from CommandSender
    // Cuz MessageCommandSender still had these methods, but no code for them
    //---------------------------

    @Nonnull
    @Override
    public Spigot spigot() {
        performOnlineCheck();
        return getPlayer().spigot();
    }


    @Override
    public boolean isPermissionSet(String name) {
        performOnlineCheck();
        return getPlayer().isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        performOnlineCheck();
        return getPlayer().isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        performOnlineCheck();
        return getPlayer().hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        performOnlineCheck();
        return getPlayer().hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        performOnlineCheck();
        return getPlayer().addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        performOnlineCheck();
        return getPlayer().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        performOnlineCheck();
        return getPlayer().addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        performOnlineCheck();
        return getPlayer().addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        performOnlineCheck();
        getPlayer().removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        performOnlineCheck();
        getPlayer().recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        performOnlineCheck();
        return getPlayer().getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        performOnlineCheck();
        return getPlayer().isOp();
    }

    @Override
    public void setOp(boolean value) {
        performOnlineCheck();
        getPlayer().setOp(value);
    }

    @Override
    public void sendMessage(BaseComponent component) {
        performOnlineCheck();
        getPlayer().sendMessage(component);
    }

    @Override
    public void sendMessage(BaseComponent... components) {
        performOnlineCheck();
        getPlayer().sendMessage(components);
    }

    @Override
    public void performCommand(String command){
        performOnlineCheck();
        getServer().dispatchCommand(this, command);
    }

    private void performOnlineCheck(){
        if(!isOnline()) throw new UserNotOnlineException(this);
    }

    @Override
    public long getNextAllowedBranchSwap() {
        return nextBranchSwapAllowed;
    }

    @Override
    public boolean performBranchSwappingCheck(){
        if(getCanSwapBranch()) return true;

        final long timUntil = getNextAllowedBranchSwap() - System.currentTimeMillis();

        sendMessage("&7You cannot currently swap branches!");
        sendMessage("&7You can swap your branch in &e" + CrownUtils.convertMillisIntoTime(timUntil) + "&7.");
        return false;
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
        if(getName().contains("Paranor")) addRank(Rank.LEGEND); //fuckin para lmao

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
        return System.currentTimeMillis() > getNextResetTime();
    }

    private void checkBranchSwapping(){
        if(canSwapBranch) return; //If you're already allowed to swap branches then who cares lol
        if(nextBranchSwapAllowed == 0 || nextBranchSwapAllowed > System.currentTimeMillis()) return; //0 means no cooldown, I think

        canSwapBranch = true;
        FtcCore.getInstance().getLogger().log(Level.INFO, getName() + "'s branch swapping allowed: " + canSwapBranch);
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
        getFile().addDefault("TimeStamps.LastLoad", System.currentTimeMillis());
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
        setCanSwapBranch(oldYaml.getBoolean("CanSwapBranch"), false);
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