package net.forthecrown.emperor.user;

import io.papermc.paper.adventure.AdventureComponent;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.admin.PunishmentEntry;
import net.forthecrown.emperor.admin.PunishmentManager;
import net.forthecrown.emperor.admin.record.PunishmentRecord;
import net.forthecrown.emperor.admin.record.PunishmentType;
import net.forthecrown.emperor.datafixers.EssToFTC;
import net.forthecrown.emperor.events.AfkListener;
import net.forthecrown.emperor.serializer.AbstractSerializer;
import net.forthecrown.emperor.user.data.SoldMaterialData;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.emperor.user.enums.*;
import net.forthecrown.emperor.utils.*;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.source.CommandSources;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
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
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

public class FtcUser extends AbstractSerializer<CrownCore> implements CrownUser {

    private final UUID base;
    private String name;
    private String lastOnlineName;
    public List<String> previousNames = new ArrayList<>();
    private Component nickname;

    public final FtcUserDataContainer dataContainer;
    public final FtcUserInteractions interactions;
    public final FtcUserGrave grave;
    public final FtcUserHomes homes;

    private Rank currentRank;
    private Set<Rank> ranks;
    private Branch branch;

    private List<Pet> pets;
    private Particle particleArrowActive;
    private List<Particle> particleArrowAvailable;
    private String particleDeathActive;
    private List<String> particleDeathAvailable;

    private int gems;
    private boolean canSwapBranch = true;
    private boolean allowsEmotes = true;
    private boolean allowsRidingPlayers = true;
    private boolean publicProfile = true;
    private boolean allowsTPA = true;
    private boolean acceptsPay = true;
    private boolean listeningToEavesdropper = false;
    private boolean vanished = false;
    private boolean afk = false;
    private boolean flying = false;
    private boolean godmode = false;
    private long totalEarnings;
    private long nextResetTime;
    private long nextBranchSwapAllowed;
    public String ip;

    private Map<Material, SoldMaterialData> matData = new HashMap<>();
    public SellAmount sellAmount;

    private Location entityLocation;

    private EntityPlayer handle;

    private Location lastLocation;
    public UserTeleport lastTeleport;
    public AfkListener afkListener;
    private long nextAllowedTeleport = 0;

    private CommandSender lastMessage;

    public FtcUser(@NotNull UUID base){
        super(base.toString(), "playerdata", CrownCore.inst());
        this.base = base;

        dataContainer = new FtcUserDataContainer(this, getFile());
        grave = new FtcUserGrave(this);
        interactions = new FtcUserInteractions(this);
        homes = new FtcUserHomes(this);

        if(fileDoesntExist) addDefaults();
        else reload();

        net.forthecrown.emperor.user.UserManager.LOADED_USERS.put(base, this);

        if(isOnline()) handle = getOnlineHandle().getHandle();
    }

    @Override
    protected void reloadFile(){
        name = getFile().getString("PlayerName");
        setRank(Rank.valueOf(getFile().getString("CurrentRank")), false);
        setBranch(Branch.valueOf(getFile().getString("Branch")));
        setCanSwapBranch(getFile().getBoolean("CanSwapBranch"), false);
        setPets(ListUtils.convertToList(getFile().getStringList("Pets"), str -> Pet.valueOf(str.toUpperCase())));
        setDeathParticle(getFile().getString("DeathParticle"));
        setParticleDeathAvailable(getFile().getStringList("ParticleDeathAvailable"));
        setAllowsRidingPlayers(getFile().getBoolean("AllowsRidingPlayers"));
        setGems(getFile().getInt("Gems"));
        setAllowsEmotes(getFile().getBoolean("AllowsEmotes"));
        setTotalEarnings(getFile().getLong("TotalEarnings"));
        setSellAmount(SellAmount.valueOf(getFile().getString("SellAmount").toUpperCase()));
        setProfilePublic(getFile().getBoolean("ProfilePublic", true));
        entityLocation = getFile().getLocation("LastLocation");
        allowsTPA = getFile().getBoolean("AllowsTPA", true);
        listeningToEavesdropper = getFile().getBoolean("EavesDropping", false);
        vanished = getFile().getBoolean("Vanished", false);
        flying = getFile().getBoolean("Flying", false);
        godmode = getFile().getBoolean("GodMode", false);
        ip = getFile().getString("IPAddress");
        acceptsPay = getFile().getBoolean("AcceptsPay", true);

        String nick = getFile().getString("NickName");
        if(nick != null) this.nickname = ChatUtils.convertString(nick);
        else this.nickname = null;

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

        ConfigurationSection homesSection = getFile().getConfigurationSection("Homes");
        if(homesSection != null) homes.loadFrom(homesSection);
        else homes.clear();

        interactions.loadMarriages(getFile().getConfigurationSection("Marriage"));

        matData.clear();
        ConfigurationSection matSection = getFile().getConfigurationSection("AmountEarned");
        if(matSection != null){
            for (String s: matSection.getKeys(false)){
                Material m = Material.valueOf(s.toUpperCase());
                int amountSold = matSection.getInt(s);

                SoldMaterialData data = new SoldMaterialData(m);
                data.setEarned(amountSold);
                data.recalculate();

                setMatData(data);
            }
        }

        updateName();

        if(totalEarnings < 0) totalEarnings = 0;

        interactions.reload(getFile().getStringList("BlockedUsers"));

        ConfigurationSection dataSec = getFile().getConfigurationSection("DataContainer");
        if(dataSec == null) dataSec = getFile().createSection("DataContainer");
        dataContainer.deserialize(dataSec);

        try {
            List<ItemStack> lists = (List<ItemStack>) getFile().getList("Grave");
            grave.setItems(lists);
        } catch (Exception ignored){}
    }

    @Override
    protected void saveFile(){
        if(deleted) return;

        getFile().set("PlayerName", getName());
        getFile().set("CurrentRank", getRank().toString());
        getFile().set("Branch", getBranch().toString());
        getFile().set("CanSwapBranch", getCanSwapBranch());
        getFile().set("DeathParticle", getDeathParticle());
        getFile().set("ParticleDeathAvailable", getParticleDeathAvailable());
        getFile().set("AllowsRidingPlayers", allowsRidingPlayers());
        getFile().set("Gems", getGems());
        getFile().set("SellAmount", getSellAmount().toString());
        getFile().set("AllowsEmotes", allowsEmotes());
        getFile().set("ProfilePublic", isProfilePublic());
        getFile().set("Grave", grave.getItems());
        getFile().set("LastLocation", entityLocation);
        getFile().set("AllowsTPA", allowsTPA);
        getFile().set("EavesDropping", listeningToEavesdropper);
        getFile().set("NickName", nickname == null ? null : ChatUtils.getPlainString(nickname));
        getFile().set("Flying", flying);
        getFile().set("GodMode", godmode);
        getFile().set("Vanished", vanished);
        getFile().set("IPAddress", ip);
        getFile().set("AcceptsPay", acceptsPay);

        getFile().set("Pets", ListUtils.convert(getPets(), Pet::toString));

        if(totalEarnings < 0) totalEarnings = 0;
        getFile().set("TotalEarnings", getTotalEarnings());

        getFile().set("TimeStamps.NextResetTime", getNextResetTime());
        if(!canSwapBranch) getFile().set("TimeStamps.NextBranchSwap", nextBranchSwapAllowed);
        else getFile().set("TimeStamps.NextBranchSwap", null);

        if(getArrowParticle() == null) getFile().set("ArrowParticle", "none");
        else getFile().set("ArrowParticle", getArrowParticle().toString());

        if(getAvailableRanks().size() > 0) getFile().set("AvailableRanks", ListUtils.convertToList(getAvailableRanks(), Rank::toString));
        if(getParticleArrowAvailable().size() > 0) getFile().set("ArrowParticleAvailable", ListUtils.convertToList(getParticleArrowAvailable(), Particle::toString));

        if(!MapUtils.isNullOrEmpty(matData)){
            Map<String, Object> serialized = new HashMap<>();

            for (SoldMaterialData d: matData.values()){
                if(d.getEarned() <= 0) continue;
                serialized.put(d.getMaterial().name().toLowerCase(), d.getEarned());
            }

            getFile().createSection("AmountEarned", serialized);
        } else getFile().set("AmountEarned", null);

        if(!homes.isEmpty()) homes.saveInto(getFile().createSection("Homes"));
        else getFile().set("Homes", null);

        getFile().set("BlockedUsers", interactions.save());

        if(!dataContainer.isEmpty()) getFile().createSection("DataContainer", dataContainer.serialize());

        Map<String, Object> marriages = interactions.serializeMarriages();
        if(marriages == null) getFile().set("Marriage", null);
        else getFile().createSection("Marriage", marriages);
    }

    public void updateName(){
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
    public void unload(){
        save();
        net.forthecrown.emperor.user.UserManager.LOADED_USERS.remove(this.getUniqueId());
        handle = null;
    }

    @Override
    public UUID getUniqueId() {
        return base;
    }

    public CraftPlayer getOnlineHandle(){
        return (CraftPlayer) Bukkit.getPlayer(base);
    }

    //If you get this while the player is online, it causes a class cast exception, use only when actually offline lol
    public CraftOfflinePlayer getOfflineHandle(){
        return (CraftOfflinePlayer) Bukkit.getOfflinePlayer(base);
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
        UserManager manager = CrownCore.getLuckPerms().getUserManager();
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

        if(addToCooldown) nextBranchSwapAllowed = System.currentTimeMillis() + CrownCore.getBranchSwapCooldown();
        else nextBranchSwapAllowed = 0;
    }

    @Override
    public List<Pet> getPets() {
        return pets;
    }

    @Override
    public void setPets(List<Pet> pets) {
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
        this.gems = Math.max(0, gems);
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
    public SoldMaterialData getMatData(Material material){
        if(!hasMatData(material)){
            SoldMaterialData data = new SoldMaterialData(material);
            setMatData(data);

            return data;
        }

        return matData.get(material);
    }

    @Override
    public boolean hasMatData(Material material){
        return matData.containsKey(material);
    }

    @Override
    public void setMatData(SoldMaterialData data){
        matData.put(data.getMaterial(), data);
    }

    @Override
    public boolean isBaron() {
        Score score = CrownCore.inst().getServer().getScoreboardManager().getMainScoreboard().getObjective("Baron").getScore(getName());
        return score.isScoreSet() && score.getScore() == 1;
    }
    @Override
    public void setBaron(boolean baron) {
        int yayNay = baron ? 1 : 0;
        CrownCore.inst().getServer().getScoreboardManager().getMainScoreboard().getObjective("Baron").getScore(getName()).setScore(yayNay);
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
        matData.clear();
        setTotalEarnings(0);

        nextResetTime = System.currentTimeMillis() + CrownCore.getUserDataResetInterval();
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
        if(CrownUtils.isNullOrBlank(s)) clearTabPrefix();
        else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + getName() + " tabprefix " + s);
    }

    @Override
    public void clearTabPrefix(){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab player " + getName() + " tabprefix ");
    }

    @Override
    public void sendMessage(@Nonnull String message){
        if(!isOnline()) return;
        sendMessage(ChatUtils.stringToVanilla(message));
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
        getOnlineHandle().sendMessage(sender, ChatFormatter.translateHexCodes(message));
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

    @Nonnull
    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isOnline() {
        return Bukkit.getPlayer(this.getUniqueId()) != null;
    }

    @Override
    public Scoreboard getScoreboard(){
        return getServer().getScoreboardManager().getMainScoreboard();
    }

    @Override
    public boolean isKing() {
        if(CrownCore.getKingship().getUniqueId() != null) return CrownCore.getKingship().getUniqueId().equals(getUniqueId());
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
            CrownCore.getKingship().set(getUniqueId());
            return;
        }

        CrownCore.getKingship().set(null);
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

    @Nonnull
    @Override
    public Spigot spigot() {
        checkOnline();
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
        checkOnline();
        return getOnlineHandle().addAttachment(plugin, name, value);
    }

    @Nonnull
    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin) {
        checkOnline();
        return getOnlineHandle().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin, @Nonnull String name, boolean value, int ticks) {
        checkOnline();
        return getOnlineHandle().addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin, int ticks) {
        checkOnline();
        return getOnlineHandle().addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@Nonnull PermissionAttachment attachment) {
        checkOnline();
        getOnlineHandle().removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        checkOnline();
        getOnlineHandle().recalculatePermissions();
    }

    @Nonnull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        checkOnline();
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

    protected void checkOnline(){
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
        sendMessage("&7You can swap your branch in &e" + ChatFormatter.convertMillisIntoTime(timUntil) + "&7.");
        return false;
    }

    @Override
    public Grave getGrave() {
        return grave;
    }

    @Override
    public Location getLocation() {
        if(!isOnline()) return entityLocation;
        return new Location(getHandle().world.getWorld(), getHandle().locX(), getHandle().locY(), getHandle().locZ(), getHandle().getBukkitYaw(), getHandle().pitch);
    }

    @Override
    public World getWorld() {
        if(!isOnline()) return entityLocation.getWorld();
        return getHandle().world.getWorld();
    }

    @Override
    public void onLeave() {
        entityLocation = getOnlineHandle().getLocation();

        if(afkListener != null){
            HandlerList.unregisterAll(afkListener);
            afkListener = null;
        }

        interactions.clearIncoming();
        interactions.clearOutgoing();

        if(lastTeleport != null) lastTeleport.interrupt(false);

        if(CrownCore.getPunishmentManager().checkJailed(getPlayer())){
            CrownCore.getJailManager().getJailListener(getPlayer()).unreg();
        }

        getFile().set("TimeStamps.LastLoad", System.currentTimeMillis());
        unload();
    }

    @Override
    public void onJoin(){
        this.handle = getOnlineHandle().getHandle();
        this.ip = getPlayer().getAddress().getHostString();
        updateVanished();
    }

    @Override
    public void onJoinLater(){
        if(EssToFTC.hasEssData(this)){
            EssToFTC ess = EssToFTC.of(this);

            ess.convert();
            ess.complete();
        }

        updateFlying();
        afk = false;

        if(!hasPermission(Permissions.CORE_ADMIN)) godmode = false;
        updateGodMode();
        updateAfk();
        updateDisplayName();

        permsCheck();
        if(shouldResetEarnings()) resetEarnings();

        getFile().set("TimeStamps.LastLoad", System.currentTimeMillis());

        PunishmentManager manager = CrownCore.getPunishmentManager();
        PunishmentEntry entry = manager.getEntry(getUniqueId());
        if(entry != null){
            PunishmentRecord record = entry.getCurrent(PunishmentType.JAIL);
            if(record == null) return;

            try {
                Key key = CrownUtils.parseKey(record.extra);
                CrownCore.getPunishmentManager().jail(key, getPlayer());
            } catch (Exception ignored) {}
        }
    }

    @Override
    public @NonNull HoverEvent<Component> asHoverEvent() {
        TextComponent.Builder text = Component.text()
                .append(name())
                .append(Component.newline());

        if(nickname != null){
            text.append(Component.text("Nickname: ").append(nickname))
                    .append(Component.newline());
        }

        if(getBranch() != Branch.DEFAULT && isProfilePublic()){
            text.append(Component.text("Branch: ").append(Component.text(getBranch().getName())));
            text.append(Component.newline());
        }

        if(getRank() != Rank.DEFAULT){
            text.append(Component.text("Rank: ").append(getRank().prefix()));
            text.append(Component.newline());
        }

        text.append(Component.text(getUniqueId().toString()));
        return HoverEvent.showText(text.build());
    }

    @Override
    public @NonNull HoverEvent<Component> asHoverEvent(@NonNull UnaryOperator<Component> op) {
        return asHoverEvent();
    }

    @Override
    public boolean hasPet(Pet pet) {
        return pets.contains(pet);
    }

    @Override
    public void addPet(Pet pet) {
        pets.add(pet);
    }

    @Override
    public void removePet(Pet pet) {
        pets.remove(pet);
    }

    @Override
    public UserTeleport getLastTeleport() {
        return lastTeleport;
    }

    @Override
    public UserTeleport createTeleport(Supplier<Location> destination, boolean tell, UserTeleport.Type type){
        return createTeleport(destination, tell, hasPermission(Permissions.TP_BYPASS), type);
    }

    @Override
    public UserTeleport createTeleport(Supplier<Location> destination, boolean tell, boolean bypassCooldown, UserTeleport.Type type){
        checkOnline();

        if(lastTeleport != null) lastTeleport.interrupt(tell);
        return lastTeleport = new UserTeleport(this, destination, bypassCooldown, type);
    }

    @Override
    public boolean isTeleporting(){
        return lastTeleport != null;
    }

    @Override
    public boolean canTeleport(){
        if(isTeleporting()) return false;
        return !Cooldown.contains(this, "Core_TeleportCooldown");
    }

    @Override
    public void onTpComplete(){
        if(!lastTeleport.shouldBypassCooldown()){
            long cooldownMillis = CrownCore.getTpCooldown() * 50;
            nextAllowedTeleport = System.currentTimeMillis() + cooldownMillis;

            Cooldown.add(this, "Core_TeleportCooldown", CrownCore.getTpCooldown());
        }

        if(!lastTeleport.isCancelled()) lastTeleport.stop();
        lastTeleport = null;
    }

    @Override
    public boolean checkTeleporting(){
        if(!canTeleport()){
            sendMessage(
                    Component.text("You can teleport again in ")
                            .color(NamedTextColor.GRAY)
                            .append(Component.text(ChatFormatter.convertMillisIntoTime(nextAllowedTeleport - System.currentTimeMillis())).color(NamedTextColor.GOLD))
            );
            return false;
        }
        return true;
    }

    @Override
    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    @Override
    public boolean allowsTPA() {
        return allowsTPA;
    }

    @Override
    public void setAllowsTPA(boolean allowsTPA) {
        this.allowsTPA = allowsTPA;
    }

    @Override
    public boolean isEavesDropping() {
        return listeningToEavesdropper;
    }

    @Override
    public void setEavesDropping(boolean listeningToSocialSpy) {
        this.listeningToEavesdropper = listeningToSocialSpy;
    }

    @Override
    public CommandSource getLastMessage() {
        if(lastMessage == null) return null;
        CommandListenerWrapper wrapper = GrenadierUtils.senderToWrapper(lastMessage);

        return CommandSources.getOrCreate(wrapper, null);
    }

    @Override
    public void setLastMessage(CommandSource lastMessage) {
        this.lastMessage = lastMessage.asBukkit();
    }

    @Override
    public void setNickname(String nick) {
        if(nick == null){
            this.nickname = null;
            if(isOnline()) updateDisplayName();
        }
        else setNickname(ChatUtils.convertString(nick, false));
    }

    @Override
    public void setNickname(Component component){
        this.nickname = component;

        if(isOnline()) updateDisplayName();
    }

    @Override
    public void updateDisplayName(){
        checkOnline();

        TabPlayer tPlayer = TABAPI.getPlayer(getUniqueId());
        if(nickname == null) tPlayer.setValuePermanently(EnumProperty.CUSTOMTABNAME, getName());
        else tPlayer.setValuePermanently(EnumProperty.CUSTOMTABNAME, ChatUtils.getString(nickname));
    }

    @Override
    public String getNickname() {
        if(nickname == null) return null;
        return ChatUtils.getString(nickname);
    }

    @Override
    public Component nickname() {
        return nickname;
    }

    @Override
    public boolean isVanished() {
        return vanished;
    }

    @Override
    public void setVanished(boolean vanished) {
        this.vanished = vanished;

        if(isOnline()) updateVanished();
    }

    @Override
    public boolean isAfk() {
        return afk;
    }

    @Override
    public void setAfk(boolean afk) {
        this.afk = afk;

        if(isOnline()) updateAfk();
    }

    @Override
    public void updateAfk() {
        checkOnline();

        TabPlayer tPlayer = TABAPI.getPlayer(getUniqueId());
        if(afk){
            tPlayer.setValuePermanently(EnumProperty.TABSUFFIX, ChatColor.GRAY + " [AFK]");
            afkListener = new AfkListener(this);
            Bukkit.getPluginManager().registerEvents(afkListener, CrownCore.inst());
        }
        else {
            tPlayer.setValuePermanently(EnumProperty.TABSUFFIX, "");

            if(afkListener != null) {
                HandlerList.unregisterAll(afkListener);
                afkListener = null;
            }
        }
    }

    @Override
    public UserHomes getHomes(){
        return homes;
    }

    @Override
    public CrownGameMode getGameMode() {
        checkOnline();
        return CrownGameMode.wrap(getPlayer().getGameMode());
    }

    @Override
    public void setGameMode(CrownGameMode gameMode) {
        checkOnline();

        getPlayer().setGameMode(gameMode.bukkit);
    }

    @Override
    public void updateVanished(){
        checkOnline();

        for (CrownUser u: net.forthecrown.emperor.user.UserManager.getOnlineUsers()){
            if(u.hasPermission(Permissions.VANISH_SEE)) continue;
            if(u.equals(this)) continue;

            if(vanished) u.getPlayer().hidePlayer(CrownCore.inst(), getPlayer());
            else u.getPlayer().showPlayer(CrownCore.inst(), getPlayer());
        }
    }

    @Override
    public boolean isFlying() {
        return flying;
    }

    @Override
    public void setFlying(boolean flying) {
        this.flying = flying;

        if(isOnline()) updateFlying();
    }

    @Override
    public void updateFlying() {
        checkOnline();

        boolean fly = getGameMode().canFly || flying;
        getPlayer().setAllowFlight(fly);
    }

    @Override
    public boolean godMode() {
        return godmode;
    }

    @Override
    public void setGodMode(boolean godMode) {
        this.godmode = godMode;

        if(isOnline()) updateGodMode();
    }

    @Override
    public void updateGodMode() {
        checkOnline();

        if(godmode){
            getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            getPlayer().setFoodLevel(20);
        }

        getPlayer().setInvulnerable(godmode);
    }

    @Override
    public UserInteractions getInteractions(){
        return interactions;
    }

    @Override
    public boolean allowsPaying(){
        return acceptsPay;
    }

    @Override
    public void setAllowsPay(boolean acceptsPay) {
        this.acceptsPay = acceptsPay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FtcUser user = (FtcUser) o;
        return user.getUniqueId().equals(getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUniqueId(), currentRank, getCanSwapBranch(), getPets(), particleArrowActive, particleDeathActive, allowsRidingPlayers, getGems(), allowsEmotes, getSellAmount(), getTotalEarnings());
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "base=" + base +
                ", name='" + name + '\'' +
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
        CrownCore.inst().getLogger().log(Level.INFO, getName() + "'s branch swapping allowed: " + canSwapBranch);
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

        getFile().addDefault("TimeStamps.NextResetTime", System.currentTimeMillis() + CrownCore.getUserDataResetInterval());
        getFile().addDefault("TimeStamps.LastLoad", System.currentTimeMillis());
        getFile().options().copyDefaults(true);

        super.save(false);
        reload();
    }
}