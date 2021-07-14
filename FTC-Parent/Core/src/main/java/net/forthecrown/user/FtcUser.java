package net.forthecrown.user;

import io.papermc.paper.adventure.AdventureComponent;
import it.unimi.dsi.fastutil.objects.*;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.PunishmentManager;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.chat.JoinInfo;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.economy.selling.UserSellResult;
import net.forthecrown.events.dynamic.AfkListener;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.source.CommandSources;
import net.forthecrown.user.data.SoldMaterialData;
import net.forthecrown.user.data.UserProperty;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.user.enums.*;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.util.Tristate;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FtcUser implements CrownUser {

    private final UUID base;
    public String name;

    public String lastOnlineName;
    public ObjectList<String> previousNames = new ObjectArrayList<>();
    public Component nickname;
    public Component currentPrefix;

    public final FtcUserDataContainer dataContainer;
    public final FtcUserInteractions interactions;
    public final FtcUserGrave grave;
    public final FtcUserHomes homes;
    public final FtcUserCosmeticData cosmeticData;

    public Rank currentRank = Rank.DEFAULT;
    public Branch branch = Branch.DEFAULT;
    public ObjectSet<Rank> ranks = new ObjectOpenHashSet<>();

    public final ObjectList<Pet> pets = new ObjectArrayList<>();
    public final ObjectSet<UserProperty> properties = new ObjectOpenHashSet<>();

    private int gems = 0;
    public boolean afk = false;
    public long totalEarnings = 0L;
    public long nextResetTime = 0L;
    public long lastLoad = 0L;
    public long nextAllowedBranchSwap = 0L;
    public String ip;

    public Object2ObjectMap<Material, SoldMaterialData> matData = new Object2ObjectOpenHashMap<>();
    public SellAmount sellAmount = SellAmount.PER_1;

    private ServerPlayer handle;
    public AfkListener afkListener;

    public Location entityLocation;
    public Location lastLocation;

    public UserTeleport lastTeleport;
    public long nextAllowedTeleport = 0L;

    private CommandSender lastMessage;

    public FtcUser(@NotNull UUID base){
        this.base = base;

        dataContainer = new FtcUserDataContainer(this);
        interactions = new FtcUserInteractions(this);
        cosmeticData = new FtcUserCosmeticData(this);
        homes = new FtcUserHomes(this);
        grave = new FtcUserGrave(this);

        reload();

        CrownUserManager.LOADED_USERS.put(base, this);

        if(isOnline()) handle = getOnlineHandle().getHandle();
    }

    @Override
    public void reload(){
        CrownCore.getUserSerializer().deserialize(this);
        updateName();
    }

    @Override
    public void save(){
        CrownCore.getUserSerializer().serialize(this);
    }

    public void updateName(){
        if(isOnline() && name != null && !name.equals(getPlayer().getName())){ //transfers all scores to new player name if player name changes
            previousNames.add(name);

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
        CrownUserManager.LOADED_USERS.remove(this.getUniqueId());
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

    public ServerPlayer getHandle() {
        if(!isOnline()) return null;
        if(handle == null) handle = getOnlineHandle().getHandle();
        return handle;
    }

    @Override
    public CommandSource getCommandSource(AbstractCommand command){
        checkOnline();
        return CommandSources.getOrCreate(getPlayer(), command);
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
    public boolean hasRank(Rank rank){
        return ranks.contains(rank);
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
    public void removeRank(Rank rank, boolean removePermission){
        ranks.remove(rank);
        if(removePermission) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + getName() + " parent remove " + rank.getLpRank());
    }

    @Override
    public Rank getRank(){
        return currentRank;
    }

    @Override
    public void setRank(Rank rank, boolean setPrefix){
        currentRank = rank;

        if(setPrefix && rank != Rank.DEFAULT) setCurrentPrefix(rank.prefix());
    }

    @Override
    public boolean canSwapBranch() {
        checkBranchSwapping();
        return !hasProperty(UserProperty.CANNOT_SWAP_BRANCH);
    }

    @Override
    public void setCanSwapBranch(boolean canSwapBranch, boolean addToCooldown) {
        setProperty(!canSwapBranch, UserProperty.CANNOT_SWAP_BRANCH);

        if(addToCooldown) nextAllowedBranchSwap = System.currentTimeMillis() + CrownCore.getBranchSwapCooldown();
        else nextAllowedBranchSwap = 0;
    }

    @Override
    public ObjectList<Pet> getPets() {
        return pets;
    }

    @Override
    public boolean hasProperty(UserProperty property){
        return properties.contains(property);
    }

    @Override
    public void addProperty(UserProperty property){
        properties.add(property);
    }

    @Override
    public void removeProperty(UserProperty property){
        properties.remove(property);
    }

    @Override
    public void setProperty(boolean add, UserProperty property){
        if(add) properties.add(property);
        else properties.remove(property);
    }

    @Override
    public boolean allowsRiding() {
        return !hasProperty(UserProperty.FORBIDS_RIDING);
    }
    @Override
    public void setAllowsRiding(boolean allows) {
        setProperty(!allows, UserProperty.FORBIDS_RIDING);
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
        return !hasProperty(UserProperty.FORBIDS_EMOTES);
    }
    @Override
    public void setAllowsEmotes(boolean allowsEmotes) {
        setProperty(!allowsEmotes, UserProperty.FORBIDS_EMOTES);
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
        Score score = getServer().getScoreboardManager().getMainScoreboard().getObjective("Baron").getScore(getName());
        return score.isScoreSet() && score.getScore() == 1;
    }
    @Override
    public void setBaron(boolean baron) {
        int yayNay = baron ? 1 : 0;
        getServer().getScoreboardManager().getMainScoreboard().getObjective("Baron").getScore(getName()).setScore(yayNay);

        if(baron) {
            addRank(Rank.BARON, true);
            addRank(Rank.BARONESS, false);
        } else {
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

        nextResetTime = System.currentTimeMillis() + CrownCore.getUserResetInterval();
        System.out.println(getName() + " earnings reset, next reset in: " + (((((getNextResetTime() - System.currentTimeMillis())/1000)/60)/60)/24) + " days");
        save();
    }

    @Nonnull
    @Override
    public String getName(){
        if(FtcUtils.isNullOrBlank(name)) name = getOfflinePlayer().getName();
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
        public void sendMessage(net.minecraft.network.chat.Component message){
        sendMessage(message, ChatType.CHAT);
    }

    @Override
    public void sendMessage(UUID id, net.minecraft.network.chat.Component message){
        sendMessage(id, message, ChatType.CHAT);
    }

    @Override
    public void sendMessage(net.minecraft.network.chat.Component message, ChatType type){
        if(!isOnline()) return;
        sendMessage(Util.NIL_UUID, message, type);
    }

    @Override
    public void sendMessage(UUID id, net.minecraft.network.chat.Component message, ChatType type){
        if(!isOnline()) return;
        sendPacket(new ClientboundChatPacket(message, type, id));
    }

    @Override
    public void sendBlockableMessage(UUID id, Component message){
        if(!isOnline()) return;
        sendMessage(id, new AdventureComponent(message), ChatType.CHAT);
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
        Kingship king = CrownCore.getKingship();
        if(king.getUniqueId() != null) return king.getUniqueId().equals(getUniqueId());
        return false;
    }

    @Override
    public void delete() {
        CrownCore.getUserSerializer().delete(this);
    }

    @Override
    public boolean isProfilePublic() {
        return !hasProperty(UserProperty.PROFILE_PRIVATE);
    }

    @Override
    public void setProfilePublic(boolean publicProfile) {
        setProperty(!publicProfile, UserProperty.PROFILE_PRIVATE);
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
        return nextAllowedBranchSwap;
    }

    @Override
    public boolean performBranchSwappingCheck(){
        if(canSwapBranch()) return true;

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
        Entity entity = getHandle(); //Inheritance mapping is a bitch, I wish it would peg me instead of ripping me to pieces with a hacksaw

        return new Location(entity.level.getWorld(), entity.getX(), entity.getY(), entity.getZ(), entity.getBukkitYaw(), entity.getXRot());
    }

    @Override
    public World getWorld() {
        if(!isOnline()) return entityLocation.getWorld();
        return ((Entity) getHandle()).level.getWorld();
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
            CrownCore.getJailManager().getListener(getPlayer()).unreg();
        }

        lastLoad = System.currentTimeMillis();
        unload();
    }

    @Override
    public boolean onJoin(){
        this.handle = getOnlineHandle().getHandle();
        this.name = getOnlineHandle().getName();

        if(!name.equalsIgnoreCase(lastOnlineName)){
            lastOnlineName = name;
            updateName();
            return true;
        }

        if(isKing()) currentPrefix = CrownCore.getKingship().isFemale() ? Kingship.queenTitle() : Kingship.kingTitle();

        getOnlineHandle().sendPlayerListHeader(CrownCore.getTabList().format());

        this.ip = getPlayer().getAddress().getHostString();
        updateVanished();
        return false;
    }

    @Override
    public void onJoinLater(){
        JoinInfo news = CrownCore.getJoinInfo();
        if(news.shouldShow()) sendMessage(news.display());

        updateFlying();
        afk = false;

        if(!hasPermission(Permissions.CORE_ADMIN)) setGodMode(false);
        updateGodMode();
        updateDisplayName();

        permsCheck();
        if(shouldResetEarnings()) resetEarnings();

        net.forthecrown.user.UserManager.updateSpectatorTab();

        lastLoad = System.currentTimeMillis();

        PunishmentManager manager = CrownCore.getPunishmentManager();
        PunishmentEntry entry = manager.getEntry(getUniqueId());
        if(entry != null){
            PunishmentRecord record = entry.getCurrent(PunishmentType.JAIL);
            if(record == null) return;

            try {
                Key key = FtcUtils.parseKey(record.extra);
                CrownCore.getPunishmentManager().jail(key, getPlayer());
            } catch (Exception ignored) {}
        }
    }

    @Override
    public Component hoverEventText(UnaryOperator<Component> operator){
        TextComponent.Builder text = Component.text()
                .append(name())
                .append(Component.newline());

        if(nickname != null){
            text
                    .append(Component.text("Nickname: ").append(nickname))
                    .append(Component.newline());
        }

        if(interactions.marriedTo != null && isProfilePublic()){
            text
                    .append(Component.text("Married to: ").append(net.forthecrown.user.UserManager.getUser(interactions.marriedTo).nickOrName()))
                    .append(Component.newline());
        }

        if(getBranch() != Branch.DEFAULT && isProfilePublic()){
            text
                    .append(Component.text("Branch: ").append(Component.text(getBranch().getName())))
                    .append(Component.newline());
        }

        if(getRank() != Rank.DEFAULT){
            text
                    .append(Component.text("Rank: ").append(getRank().prefix()))
                    .append(Component.newline());
        }

        text.append(Component.text(getUniqueId().toString()));
        return operator.apply(text.build());
    }

    @Override
    public @NonNull HoverEvent<Component> asHoverEvent(@NonNull UnaryOperator<Component> op) {
        return HoverEvent.showText(hoverEventText(op));
    }

    @Override
    public void updateDisplayName(){
        checkOnline();
        Component displayName = listDisplayName();
        getOnlineHandle().playerListName(displayName);
    }

    @Override
    public Component listDisplayName(){
        return Component.text()
                .style(ChatFormatter.nonItalic(NamedTextColor.WHITE))
                .append(getCurrentPrefix())
                .append(nickDisplayName())
                .append(isAfk() ? ChatFormatter.AFK_SUFFIX : Component.empty())
                .build();
    }

    @Override
    public Component getCurrentPrefix(){
        if(currentPrefix != null) return currentPrefix;
        if(currentRank != Rank.DEFAULT) return currentRank.prefix();

        return Component.empty();
    }

    @Override
    public void setCurrentPrefix(Component component){
        this.currentPrefix = component;

        if(isOnline()) updateDisplayName();
    }

    @Override
    public void setLastOnlineName(String lastOnlineName) {
        this.lastOnlineName = lastOnlineName;
    }

    @Override
    public String getLastOnlineName() {
        return lastOnlineName;
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
        return !hasProperty(UserProperty.FORBIDS_TPA);
    }

    @Override
    public void setAllowsTPA(boolean allowsTPA) {
        setProperty(!allowsTPA, UserProperty.FORBIDS_TPA);
    }

    @Override
    public boolean isEavesDropping() {
        return hasProperty(UserProperty.LISTENING_TO_EAVESDROPPER);
    }

    @Override
    public void setEavesDropping(boolean listening) {
        setProperty(listening, UserProperty.LISTENING_TO_EAVESDROPPER);
    }

    @Override
    public CommandSource getLastMessage() {
        if(lastMessage == null) return null;
        CommandSourceStack wrapper = GrenadierUtils.senderToWrapper(lastMessage);

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
        }
        else setNickname(ChatUtils.convertString(nick, false));
    }

    @Override
    public void setNickname(Component component){
        this.nickname = component;
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
        return hasProperty(UserProperty.VANISHED);
    }

    @Override
    public void setVanished(boolean vanished) {
        setProperty(vanished, UserProperty.VANISHED);

        if(isOnline()) updateVanished();
    }

    @Override
    public boolean isAfk() {
        return afk;
    }

    @Override
    public void setAfk(boolean afk) {
        this.afk = afk;

        if(isOnline()){
            updateDisplayName();
            updateAfk();
        }
    }

    @Override
    public UserHomes getHomes(){
        return homes;
    }

    @Override
    public FtcGameMode getGameMode() {
        checkOnline();
        return FtcGameMode.wrap(getPlayer().getGameMode());
    }

    @Override
    public void setGameMode(FtcGameMode gameMode) {
        checkOnline();

        getOnlineHandle().setGameMode(gameMode.bukkit);

        net.forthecrown.user.UserManager.updateSpectatorTab();
    }

    @Override
    public void updateVanished(){
        checkOnline();

        for (CrownUser u: net.forthecrown.user.UserManager.getOnlineUsers()){
            if(u.hasPermission(Permissions.VANISH_SEE)) continue;
            if(u.equals(this)) continue;

            if(isVanished()) u.getPlayer().hidePlayer(CrownCore.inst(), getPlayer());
            else u.getPlayer().showPlayer(CrownCore.inst(), getPlayer());
        }
    }

    @Override
    public void updateAfk(){
        checkOnline();

        if(afk){
            afkListener = new AfkListener(this);
            Bukkit.getPluginManager().registerEvents(afkListener, CrownCore.inst());
        } else {
            if(afkListener != null) HandlerList.unregisterAll(afkListener);
            afkListener = null;
        }
    }

    @Override
    public boolean isFlying() {
        return hasProperty(UserProperty.FLYING);
    }

    @Override
    public void setFlying(boolean flying) {
        setProperty(flying, UserProperty.FLYING);

        if(isOnline()) updateFlying();
    }

    @Override
    public void updateFlying() {
        checkOnline();

        boolean fly = getGameMode().canFly || isFlying();
        getPlayer().setAllowFlight(fly);
    }

    @Override
    public boolean godMode() {
        return hasProperty(UserProperty.GOD_MODE);
    }

    @Override
    public void setGodMode(boolean godMode) {
        setProperty(godMode, UserProperty.GOD_MODE);

        if(isOnline()) updateGodMode();
    }

    @Override
    public void updateGodMode() {
        checkOnline();

        if(godMode()){
            getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            getPlayer().setFoodLevel(20);
        }

        getPlayer().setInvulnerable(godMode());
    }

    @Override
    public CosmeticData getCosmeticData() {
        return cosmeticData;
    }

    @Override
    public UserInteractions getInteractions(){
        return interactions;
    }

    @Override
    public boolean allowsPaying(){
        return !hasProperty(UserProperty.FORBIDS_PAY);
    }

    @Override
    public void setAllowsPay(boolean acceptsPay) {
        setProperty(!acceptsPay, UserProperty.FORBIDS_PAY);
    }

    @Override
    public UserSellResult sellMaterial(Material material, int targetAmount) {
        Validate.isTrue(targetAmount != 0, "Target amount cannot be 0");
        Validate.isTrue(targetAmount > -2, "Target amount cannot be less than -1");
        Validate.isTrue(targetAmount <= material.getMaxStackSize(), "Cannot remove more than a stack of one type");

        int foundAmount = 0;
        int filteredTarget = Math.max(1, targetAmount);
        PlayerInventory inv = getPlayer().getInventory();

        ItemStack item = new ItemStack(material, filteredTarget);

        if(!inv.containsAtLeast(item, filteredTarget)) {
            return UserSellResult.foundNone(this, targetAmount, material);
        }

        if(targetAmount == -1) {
            for (ItemStack i: inv) {
                if(FtcUtils.isItemEmpty(i)) continue;
                if(i.getType() != material) continue;

                foundAmount += i.getAmount();
                inv.removeItemAnySlot(i);
            }
        } else {
            inv.removeItemAnySlot(item);
            foundAmount = filteredTarget;
        }

        return new UserSellResult(foundAmount, targetAmount, this, material);
    }

    @Override
    public void sendActionBar(@NonNull Component message) {
        checkOnline();
        sendMessage(new AdventureComponent(message), ChatType.GAME_INFO);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NonNull Component header, @NonNull Component footer) {
        checkOnline();
        getPlayer().sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void showTitle(@NonNull Title title) {
        checkOnline();
        getPlayer().showTitle(title);
    }

    @Override
    public void clearTitle() {
        checkOnline();
        getPlayer().clearTitle();
    }

    @Override
    public void resetTitle() {
        checkOnline();
        getPlayer().resetTitle();
    }

    @Override
    public void showBossBar(@NonNull BossBar bar) {
        checkOnline();
        getPlayer().showBossBar(bar);
    }

    @Override
    public void hideBossBar(@NonNull BossBar bar) {
        checkOnline();
        getPlayer().hideBossBar(bar);
    }

    @Override
    public void playSound(@NonNull Sound sound) {
        checkOnline();
        getPlayer().playSound(sound);
    }

    @Override
    public void playSound(@NonNull Sound sound, double x, double y, double z) {
        checkOnline();
        getPlayer().playSound(sound, x, y, z);
    }

    @Override
    public void stopSound(@NonNull SoundStop stop) {
        checkOnline();
        getPlayer().stopSound(stop);
    }

    @Override
    public void openBook(@NonNull Book book) {
        checkOnline();
        getPlayer().openBook(book);
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
        return new HashCodeBuilder(17, 37)
                .append(base)
                .append(getBranch())
                .append(getGems())
                .append(properties)
                .append(isAfk())
                .append(getTotalEarnings())
                .append(getNextResetTime())
                .append(lastLoad)
                .append(getNextAllowedBranchSwap())
                .append(nextAllowedTeleport)
                .toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "base=" + base +
                ",name='" + name + '\'' +
                '}';
    }

    protected void permsCheck(){
        if(!isOnline()) return;
        if(getName().contains("Paranor") && !hasRank(Rank.LEGEND)) addRank(Rank.LEGEND); //fuckin para lmao

        if(isBaron() && !hasRank(Rank.BARON)){
            addRank(Rank.BARON, true);
            addRank(Rank.BARONESS, false);
        }

        if(getPlayer().hasPermission(Permissions.DONATOR_3) && (!hasRank(Rank.PRINCE) || !hasRank(Rank.ADMIRAL))){
            addRank(Rank.PRINCE, true);
            addRank(Rank.PRINCESS, false);
            addRank(Rank.ADMIRAL, false);
        }

        if(getPlayer().hasPermission(Permissions.DONATOR_2) && (!hasRank(Rank.DUKE) || !hasRank(Rank.CAPTAIN))){
            addRank(Rank.DUKE, true);
            addRank(Rank.DUCHESS, false);
            addRank(Rank.CAPTAIN, false);
        }

        if(getPlayer().hasPermission(Permissions.DONATOR_1) && !hasRank(Rank.LORD)){
            addRank(Rank.LORD, true);
            addRank(Rank.LADY, false);
        }
    }

    protected boolean shouldResetEarnings(){
        return System.currentTimeMillis() > getNextResetTime();
    }

    private void checkBranchSwapping(){
        setProperty(nextAllowedBranchSwap != 0 && nextAllowedBranchSwap <= System.currentTimeMillis(), UserProperty.CANNOT_SWAP_BRANCH);
    }

    protected void sendPacket(Packet<?> packet){
        checkOnline();
        Connection connection = getHandle().connection.connection;
        connection.send(packet);
    }
}