package net.forthecrown.core.files;

import io.papermc.paper.adventure.AdventureComponent;
import io.papermc.paper.adventure.PaperAdventure;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.Grave;
import net.forthecrown.core.api.UserDataContainer;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import net.forthecrown.core.exceptions.UserNotOnlineException;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.core.utils.MapUtils;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.*;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

public class FtcUser extends AbstractSerializer<FtcCore> implements CrownUser {

    private final UUID base;
    private String name;

    private final FtcUserDataContainer dataContainer;
    private final FtcUserGrave grave;

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
    private boolean publicProfile = true;
    private long totalEarnings;
    private long nextResetTime;
    private long nextBranchSwapAllowed;

    private Map<Material, Short> itemPrices = new HashMap<>();
    private Map<Material, Integer> amountEarned = new HashMap<>();
    private SellAmount sellAmount;

    private Location lastKnownLocation;
    private EntityPlayer handle;

    public FtcUser(@NotNull UUID base){
        super(base.toString(), "playerdata", FtcCore.getInstance());
        this.base = base;

        dataContainer = new FtcUserDataContainer(this);
        grave = new FtcUserGrave(this);

        if(fileDoesntExist) addDefaults();
        else reload();
        if(shouldResetEarnings()) resetEarnings();

        net.forthecrown.core.api.UserManager.LOADED_USERS.put(base, this);
        permsCheck();

        if(isOnline()) handle = getOnlineHandle().getHandle();
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
        setProfilePublic(getFile().getBoolean("ProfilePublic", true));
        lastKnownLocation = getFile().getLocation("LastLocation");

        setNextResetTime(getFile().getLong("TimeStamps.NextResetTime"));

        nextBranchSwapAllowed = getFile().getLong("TimeStamps.NextBranchSwap");
        if(nextBranchSwapAllowed != 0) checkBranchSwapping();

        if(!getFile().getString("ArrowParticle").contains("none")) setArrowParticle(Particle.valueOf(getFile().getString("ArrowParticle")));
        else setArrowParticle(null);

        if(getFile().getList("AvailableRanks") != null){
            setAvailableRanks(ListUtils.convertToSet(getFile().getStringList("AvailableRanks"), Rank::valueOf));
        } else setAvailableRanks(new HashSet<>());

        if(getFile().getList("ArrowParticleAvailable") != null){
            setParticleArrowAvailable(ListUtils.convertToList(getFile().getStringList("ArrowParticleAvailable"), Particle::valueOf));
        } else setParticleDeathAvailable(new ArrayList<>());

        itemPrices.clear();
        if(getFile().getConfigurationSection("ItemPrices") != null && getFile().getConfigurationSection("ItemPrices").getKeys(true).size() > 0){
            Map<String, Object> map = getFile().getConfigurationSection("ItemPrices").getValues(false);
            setItemPrices(MapUtils.convert(map, Material::valueOf, o -> Short.parseShort(o.toString())));
        }

        amountEarned.clear();
        if(getFile().getConfigurationSection("AmountEarned") != null && getFile().getConfigurationSection("AmountEarned").getKeys(true).size() > 0){
            Map<String, Object> map = getFile().getConfigurationSection("AmountEarned").getValues(false);
            setAmountEarnedMap(MapUtils.convert(map, Material::valueOf, o -> Integer.parseInt(o.toString())));
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

        if(totalEarnings < 0) totalEarnings = 0;

        ConfigurationSection dataSec = getFile().getConfigurationSection("DataContainer");
        if(dataSec == null) dataSec = getFile().createSection("DataContainer");
        dataContainer.deserialize(dataSec);

        try {
            List<ItemStack> lists = (List<ItemStack>) getFile().getList("Grave");
            grave.setItems(lists);
        } catch (Exception ignored){}
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
        getFile().set("AllowsEmotes", allowsEmotes());
        getFile().set("ProfilePublic", isProfilePublic());
        getFile().set("Grave", grave.getItems());
        getFile().set("LastLocation", getLocation());

        if(totalEarnings < 0) totalEarnings = 0;
        getFile().set("TotalEarnings", getTotalEarnings());

        getFile().set("TimeStamps.NextResetTime", getNextResetTime());
        getFile().set("TimeStamps.LastLoad", System.currentTimeMillis());
        if(!canSwapBranch) getFile().set("TimeStamps.NextBranchSwap", nextBranchSwapAllowed);
        else getFile().set("TimeStamps.NextBranchSwap", null);

        if(getArrowParticle() == null) getFile().set("ArrowParticle", "none");
        else getFile().set("ArrowParticle", getArrowParticle().toString());

        if(getAvailableRanks().size() > 0) getFile().set("AvailableRanks", ListUtils.convertToList(getAvailableRanks(), Rank::toString));
        if(getParticleArrowAvailable().size() > 0) getFile().set("ArrowParticleAvailable", ListUtils.convertToList(getParticleArrowAvailable(), Particle::toString));
        if(getItemPrices().size() > 0) getFile().createSection("ItemPrices", MapUtils.convertKeys(getItemPrices(), Material::toString));
        if(getAmountEarnedMap().size() > 0) getFile().createSection("AmountEarned", MapUtils.convertKeys(getAmountEarnedMap(), Material::toString));

        if(!dataContainer.isEmpty()) getFile().createSection("DataContainer", dataContainer.serialize());

        super.save();
    }

    @Override
    public void unload(){
        save();
        net.forthecrown.core.api.UserManager.LOADED_USERS.remove(this.getUniqueId());
        handle = null;
    }

    @Override
    public short configurePriceForItem(Material item){
        short startPrice = FtcCore.getItemPrice(item);
        int amountEarned1 = getAmountEarned(item);

        if(amountEarned1 <= 0) return startPrice;

        return (short) Math.ceil( (1+startPrice)*Math.exp( -amountEarned1*Math.log(1+startPrice)/500000 )-1 );
    }

    @Override
    public UUID getUniqueId() {
        return base;
    }

    public CraftPlayer getOnlineHandle(){
        return (CraftPlayer) Bukkit.getPlayer(getBase());
    }

    //If you get this while the player is online, it causes a class cast exception, use only when actually offline lol
    public CraftOfflinePlayer getOfflineHandle(){
        return (CraftOfflinePlayer) Bukkit.getOfflinePlayer(getBase());
    }

    public EntityPlayer getHandle() {
        if(!isOnline()) return null;
        if(handle == null) handle = getOnlineHandle().getHandle();
        return handle;
    }

    @Override
    public Player getPlayer(){
        return getOnlineHandle();
    }

    @Override
    public OfflinePlayer getOfflinePlayer(){
        if(isOnline()) return getOnlineHandle();
        return getOfflineHandle();
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
        return ranks.contains(rank);
    }

    @Override
    public void addRank(Rank rank){
        addRank(rank, true);
    }

    @Override
    public void addRank(Rank rank, boolean givePermission){
        ranks.add(rank);
        if(givePermission) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + getName() + " parent add " + rank.getLpRank());
    }

    private User getLuckPermsUser(){
        UserManager manager = FtcCore.LUCK_PERMS.getUserManager();
        if(!manager.isLoaded(getUniqueId())) manager.loadUser(getUniqueId());
        return manager.getUser(getUniqueId());
    }

    private CachedPermissionData getPerms(){
        return getLuckPermsUser().getCachedData().getPermissionData();
    }

    @Override
    public void removeRank(Rank rank){
        removeRank(rank, true);
    }

    @Override
    public void removeRank(Rank rank, boolean removePermission){
        ranks.remove(rank);
        if(removePermission) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + getName() + " parent remove " + rank.getLpRank());
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

        if(setPrefix && rank != Rank.DEFAULT) setTabPrefix(rank.getColorlessPrefix());
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
    public Short getItemPrice(Material item){
        return itemPrices.getOrDefault(item, FtcCore.getItemPrice(item));
    }
    @Override
    public void setItemPrice(Material item, short price){
        itemPrices.put(item, price);
    }

    @Override
    public Map<Material, Short> getItemPrices() {
        return itemPrices;
    }
    @Override
    public void setItemPrices(@Nonnull Map<Material, Short> itemPrices) {
        this.itemPrices = itemPrices;
    }

    @Override
    public Integer getAmountEarned(Material material){
        return amountEarned.getOrDefault(material, 0);
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
            addRank(Rank.BARON, true);
            addRank(Rank.BARONESS, false);
        }
        else{
             removeRank(Rank.BARON, true);
             removeRank(Rank.BARONESS, false);
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

        nextResetTime = System.currentTimeMillis() + FtcCore.getUserDataResetInterval();
        System.out.println(getName() + " earnings reset, next reset in: " + (((((getNextResetTime() - System.currentTimeMillis())/1000)/60)/60)/24) + " days");
        save();
    }

    @Nonnull
    @Override
    public String getName(){
        if(CrownUtils.isNullOrBlank(name)) name = getOfflinePlayer().getName();
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
        sendMessage(ComponentUtils.stringToVanilla(message));
    }

    @Override
    public void sendMessage(@Nonnull String... messages){
        for (String s: messages){
            sendMessage(s);
        }
    }

    @Override
    public void sendMessage(@NonNull Component message) {
        if(!isOnline()) return;
        sendMessage(new AdventureComponent(message));
    }

    @Override
    public void sendMessage(UUID sender, @Nonnull String message) {
        if(!isOnline()) return;
        getOnlineHandle().sendMessage(sender, CrownUtils.translateHexCodes(message));
    }

    @Override
    public void sendMessage(UUID sender, String[] messages) {
        for (String s: messages){
            sendMessage(sender, s);
        }
    }

    @Override
        public void sendMessage(IChatBaseComponent message){
        sendMessage(message, ChatMessageType.CHAT);
    }

    @Override
    public void sendMessage(UUID id, IChatBaseComponent message){
        sendMessage(id, message, ChatMessageType.CHAT);
    }

    @Override
    public void sendMessage(IChatBaseComponent message, ChatMessageType type){
        if(!isOnline()) return;
        getHandle().playerConnection.sendPacket(new PacketPlayOutChat(message, type, SystemUtils.b));
    }

    @Override
    public void sendMessage(UUID id, IChatBaseComponent message, ChatMessageType type){
        if(!isOnline()) return;
        getHandle().playerConnection.sendPacket(new PacketPlayOutChat(message, type, id));
    }

    @Override
    public void sendAdminMessage(CrownCommandBuilder command, CommandSender sender, Component message){
        if(!command.testPermissionSilent(this)) return;
        Component component = Component.text("[ " + sender.getName() + ": ")
                .style(Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC))
                .append(message)
                .append(Component.text("]"));
        sendMessage(component);
    }

    @Nonnull
    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isOnline() {
        boolean result = Bukkit.getPlayer(this.getUniqueId()) != null;
        return result;
    }

    @Override
    public Scoreboard getScoreboard(){
        return getServer().getScoreboardManager().getMainScoreboard();
    }

    @Override
    public boolean isKing() {
        if(FtcCore.getKing() != null) return FtcCore.getKing().equals(getBase());
        return false;
    }

    @Override
    public void setKing(boolean king, boolean setPrefix) {
        setKing(king, setPrefix, false);
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
        if(setPrefix) setTabPrefix(getRank() == Rank.DEFAULT ? "" : getRank().getColorlessPrefix());
    }

    @Override
    public void setKing(boolean king) {
        setKing(king, false, false);
    }

    @Override
    public void delete() {
        super.delete();
    }

    @Override
    public boolean isProfilePublic() {
        return publicProfile;
    }

    @Override
    public void setProfilePublic(boolean publicProfile) {
        this.publicProfile = publicProfile;
    }

    @Override
    public UserDataContainer getDataContainer() {
        return dataContainer;
    }

    //---------------------------
    // The following methods are inherited from CommandSender
    // Cuz CommandSender still had these methods, but no code for them
    //---------------------------

    @Nonnull
    @Override
    public Spigot spigot() {
        performOnlineCheck();
        return getOnlineHandle().spigot();
    }


    @Override
    public boolean isPermissionSet(@Nonnull String name) {
        if(!isOnline()) return getPerms().checkPermission(name) != Tristate.UNDEFINED;
        return getOnlineHandle().isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(@Nonnull Permission perm) {
        if(isOnline()) return getOnlineHandle().hasPermission(perm.getName());
        return getPerms().checkPermission(perm.getName()).asBoolean();
    }

    @Override
    public boolean hasPermission(@Nonnull String name) {
        if(isOnline()) return getOnlineHandle().hasPermission(name);
        return getPerms().checkPermission(name).asBoolean();
    }

    @Override
    public boolean hasPermission(@Nonnull Permission perm) {
        return hasPermission(perm.getName());
    }

    @Nonnull
    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin, @Nonnull String name, boolean value) {
        performOnlineCheck();
        return getOnlineHandle().addAttachment(plugin, name, value);
    }

    @Nonnull
    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin) {
        performOnlineCheck();
        return getOnlineHandle().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin, @Nonnull String name, boolean value, int ticks) {
        performOnlineCheck();
        return getOnlineHandle().addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin, int ticks) {
        performOnlineCheck();
        return getOnlineHandle().addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@Nonnull PermissionAttachment attachment) {
        performOnlineCheck();
        getOnlineHandle().removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        performOnlineCheck();
        getOnlineHandle().recalculatePermissions();
    }

    @Nonnull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        performOnlineCheck();
        return getOnlineHandle().getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return getOfflinePlayer().isOp();
    }

    @Override
    public void setOp(boolean value) {
        getOfflinePlayer().setOp(value);
    }

    @Override
    public void sendMessage(@NotNull Identity identity, @NotNull Component message, @NotNull MessageType type) {
        getOnlineHandle().sendMessage(identity, message, type);
    }

    protected void performOnlineCheck(){
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
    public Grave getGrave() {
        return grave;
    }

    @Override
    public Location getLocation() {
        if(!isOnline()) return lastKnownLocation;
        return new Location(getWorld(), handle.locX(), handle.locY(), handle.locZ(), handle.getBukkitYaw(), handle.pitch);
    }

    @Override
    public World getWorld() {
        if(!isOnline()) return lastKnownLocation.getWorld();
        return handle.world.getWorld();
    }

    @Override
    public void onLeave() {
        lastKnownLocation = getOnlineHandle().getLocation();
        unload();
    }

    @Override
    public @NonNull HoverEvent<Component> asHoverEvent() {
        Component text = Component.text()
                .append(name())
                .append(Component.newline())
                .build();

        if(getBranch() != Branch.DEFAULT && isProfilePublic()){
            text = text.append(Component.text("Branch: ").append(Component.text(branch.getName())));
            text = text.append(Component.newline());
        }

        if(getRank() != Rank.DEFAULT){
            text = text.append(Component.text("Rank: ").append(getRank().prefix()));
            text = text.append(Component.newline());
        }

        text = text.append(Component.text("Type: User")).append(Component.newline());
        text = text.append(Component.text(getBase().toString()));
        return HoverEvent.showText(text);
    }

    @Override
    public @NonNull HoverEvent<Component> asHoverEvent(@NonNull UnaryOperator<Component> op) {
        return asHoverEvent();
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
        return getClass().getName() + "{" +
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

    protected void permsCheck(){
        if(!isOnline()) return;
        if(getName().contains("Paranor") && !hasRank(Rank.LEGEND)) addRank(Rank.LEGEND); //fuckin para lmao

        if(isBaron() && !hasRank(Rank.BARON)){
            addRank(Rank.BARON, true);
            addRank(Rank.BARONESS, false);
        }

        if(getPlayer().hasPermission("ftc.donator3") && (!hasRank(Rank.PRINCE) || !hasRank(Rank.ADMIRAL))){
            addRank(Rank.PRINCE, true);
            addRank(Rank.PRINCESS, false);
            addRank(Rank.ADMIRAL, false);
        }

        if(getPlayer().hasPermission("ftc.donator2") && (!hasRank(Rank.DUKE) || !hasRank(Rank.CAPTAIN))){
            addRank(Rank.DUKE, true);
            addRank(Rank.DUCHESS, false);
            addRank(Rank.CAPTAIN, false);
        }

        if(getPlayer().hasPermission("ftc.donator1") && !hasRank(Rank.LORD)){
            addRank(Rank.LORD, true);
            addRank(Rank.LADY, false);
        }
    }

    protected boolean shouldResetEarnings(){
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
        getFile().addDefault("ProfilePublic", true);

        getFile().addDefault("TimeStamps.NextResetTime", System.currentTimeMillis() + FtcCore.getUserDataResetInterval());
        getFile().addDefault("TimeStamps.LastLoad", System.currentTimeMillis());
        getFile().options().copyDefaults(true);

        super.save();
        reload();
    }
}