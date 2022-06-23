package net.forthecrown.user;

import io.papermc.paper.adventure.AdventureComponent;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.objects.*;
import net.forthecrown.commands.CommandArkBox;
import net.forthecrown.core.*;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.battlepass.challenges.Challenges;
import net.forthecrown.core.chat.*;
import net.forthecrown.economy.selling.ItemFilter;
import net.forthecrown.events.dynamic.AfkListener;
import net.forthecrown.events.dynamic.RegionVisitListener;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.VanillaAccess;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FtcUser implements CrownUser {

    //Identifiers
    private final UUID uniqueId;
    public String name;

    //Display and name info
    public String lastOnlineName;
    public final ObjectList<String> previousNames = new ObjectArrayList<>();
    public Component nickname;
    public Component currentPrefix;

    //Attachments
    public final FtcUserMarketData marketData;
    public final FtcUserDataContainer dataContainer;
    public final FtcUserInteractions interactions;
    public final FtcUserCosmeticData cosmeticData;
    public final FtcUserMail mail;
    public final FtcUserHomes homes;

    //Rank stuff
    public RankTier tier = RankTier.NONE;
    public RankTitle currentTitle = RankTitle.DEFAULT;
    public ObjectSet<RankTitle> titles = new ObjectOpenHashSet<>();

    public final ObjectSet<UserPref> prefs = new ObjectOpenHashSet<>();

    //Primitive variables, idk
    private int gems = 0;
    public boolean afk = false;
    public boolean hulkSmashing = false;
    public long totalEarnings = 0L;
    public long nextResetTime = 0L;
    public long lastLoad = 0L;
    public long lastGuildPassDonation = 0L;
    public String ip;

    //Economy stuff
    public Object2ObjectMap<Material, SoldMaterialData> matData = new Object2ObjectOpenHashMap<>();
    public SellAmount sellAmount = SellAmount.PER_1;
    public final ItemFilter filter = new ItemFilter();

    //NMS handle
    private ServerPlayer handle;

    //Region visit listener
    public RegionVisitListener visitListener;

    public UserVanishTicker vanishTicker;

    //Afk stuff
    public AfkListener afkListener;
    public String afkReason;

    //Locations
    public Location entityLocation; // Used for tracking the player's position
    public Location lastLocation;   // Used by /back

    //Teleport stuff
    public UserTeleport lastTeleport;
    public long nextAllowedTeleport = 0L;

    //Last command sender they sent or received a message from
    private CommandSender lastMessage;

    public FtcUser(@NotNull UUID uniqueId){
        this.uniqueId = uniqueId;

        marketData = new FtcUserMarketData(this);
        dataContainer = new FtcUserDataContainer(this);
        interactions = new FtcUserInteractions(this);
        cosmeticData = new FtcUserCosmeticData(this);
        mail = new FtcUserMail(this);
        homes = new FtcUserHomes(this);

        reload();

        if(isOnline()) handle = getOnlineHandle().getHandle();
    }

    @Override
    public void reload(){
        Crown.getUserManager().getSerializer().deserialize(this);
        updateName(lastOnlineName);
    }

    @Override
    public void save(){
        Crown.getUserManager().getSerializer().serialize(this);
    }

    public void updateName(String last){
        if(isOnline() && last != null && !last.equals(getPlayer().getName())){ //transfers all scores to new player name if player name changes
            UserCache cache = Crown.getUserManager().getCache();
            cache.onNameChange(cache.getEntry(getUniqueId()), getPlayer().getName());

            Scoreboard scoreboard = getPlayer().getScoreboard();

            for (Objective obj : scoreboard.getObjectives()){
                if(!obj.getScore(last).isScoreSet()) continue;

                obj.getScore(getPlayer().getName()).setScore(obj.getScore(last).getScore());
                obj.getScore(last).resetScore();
            }
        }
    }

    @Override
    public void unload(){
        save();
        FtcUserManager.LOADED_USERS.remove(getUniqueId());
        handle = null;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    public CraftPlayer getOnlineHandle(){
        return (CraftPlayer) Bukkit.getPlayer(uniqueId);
    }

    //If you get this while the player is online, it causes a class cast exception, use only when actually offline lol
    public CraftOfflinePlayer getOfflineHandle(){
        return (CraftOfflinePlayer) Bukkit.getOfflinePlayer(uniqueId);
    }

    public ServerPlayer getHandle() {
        if(!isOnline()) return null;
        if(handle == null) handle = getOnlineHandle().getHandle();
        return handle;
    }

    @Override
    public CommandSource getCommandSource(AbstractCommand command){
        ensureOnline();
        return GrenadierUtils.wrap(GrenadierUtils.senderToWrapper(getPlayer()), command);
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
    public UserHomes getHomes(){
        return homes;
    }

    @Override
    public UserMail getMail() {
        return mail;
    }

    @Override
    public CosmeticData getCosmeticData() {
        return cosmeticData;
    }

    @Override
    public UserMarketData getMarketData() {
        return marketData;
    }

    @Override
    public UserInteractions getInteractions(){
        return interactions;
    }

    @Override
    public UserDataContainer getDataContainer() {
        return dataContainer;
    }

    @Override
    public boolean hasPref(UserPref property){
        return prefs.contains(property);
    }

    @Override
    public void addPref(UserPref property){
        prefs.add(property);
    }

    @Override
    public void removePref(UserPref property){
        prefs.remove(property);
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
    public RankTier getRankTier() {
        return tier;
    }

    @Override
    public void setRankTier(RankTier tier, boolean givePermission) {
        if(givePermission) {
            if(getRankTier() != RankTier.NONE) {
                removeDefaults(this.tier);
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "lp user " + getName() + " parent remove " + getRankTier().luckPermsGroup
                );
            }

            addDefaults(tier);
            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "lp user " + getName() + " parent add " + tier.luckPermsGroup
            );
        }

        this.tier = tier;
    }

    private void addDefaults(RankTier tier) {
        for (RankTitle t: tier.getApplicableDefaults()) {
            addTitle(t, false, false);
        }
    }

    private void removeDefaults(RankTier tier) {
        for (RankTitle t: tier.getApplicableDefaults()) {
            removeTitle(t);
        }
    }

    @Override
    public RankTitle getTitle() {
        return currentTitle;
    }

    @Override
    public void setTitle(RankTitle title) {
        currentTitle = title;

        if(isOnline()) updateTabName();
    }

    @Override
    public ObjectSet<RankTitle> getAvailableTitles() {
        return titles;
    }

    @Override
    public void addTitle(RankTitle title, boolean givePermissions, boolean setTierIfHigher) {
        titles.add(title);

        if(setTierIfHigher && title.getTier().isHigherTierThan(getRankTier())) {
            setRankTier(title.getTier(), givePermissions);
        }
    }

    @Override
    public void removeTitle(RankTitle title) {
        titles.remove(title);
    }

    @Override
    public ItemFilter getSellShopFilter() {
        return filter;
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

        long interval = FtcVars.userDataResetInterval.get();
        nextResetTime = System.currentTimeMillis() + interval;

        Crown.logger().info("{} earnings reset, next reset in {}", getName(), new TimePrinter(interval).printString());

        save();
    }

    @Nonnull
    @Override
    public String getName(){
        if(FtcUtils.isNullOrBlank(name)) return name = getOfflinePlayer().getName();

        return name;
    }

    @Override
    public void sendMessage(@Nonnull String message){
        if(!isOnline()) return;
        sendMessage(ChatUtils.stringToVanilla(message));
    }

    @Override
    public void sendMessage(@Nonnull String... messages){
        for (String s: messages) sendMessage(s);
    }

    @Override
    public void sendMessage(@NonNull Component message) {
        if(!isOnline()) return;
        Component coolComponent = GlobalTranslator.render(message, getPlayer().locale());
        sendMessage(new AdventureComponent(coolComponent));
    }

    @Override
    public void sendMessage(net.minecraft.network.chat.Component message) {
        if(!isOnline()) return;
        sendMessage(message, ChatType.SYSTEM);
    }

    @Override
    public void sendMessage(net.minecraft.network.chat.Component message, ChatType type) {
        if(!isOnline()) return;
        sendMessage(message, type);
    }

    public void sendMessage(net.minecraft.network.chat.Component message, ResourceKey<ChatType> type) {
        if(!isOnline()) return;
        int id = convert(type); // ChatType -> int
        sendPacket(new ClientboundSystemChatPacket(message, id));
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        if (!isOnline()) return;
        if (sender == null) sendMessage(ChatUtils.stringToVanilla(message));
        else sendMessage(sender, ChatUtils.stringToVanilla(message));
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
        if (sender == null)
            sendMessage(messages);
        else
            for (String s: messages) sendMessage(sender, s);
    }

    @Override
    public void sendMessage(UUID sender, net.minecraft.network.chat.Component message) {
        sendMessage(sender, message, ChatType.SYSTEM);
    }

    @Override
    public void sendMessage(UUID sender, net.minecraft.network.chat.Component message, ChatType type) {
        sendMessage(sender, message, type);
    }

    public void sendMessage(UUID sender, net.minecraft.network.chat.Component message, ResourceKey<ChatType> type) {
        if(!isOnline()) return;
        int id = convert(type); // ChatType -> int
        sendPacket(new ClientboundPlayerChatPacket(PaperAdventure.asAdventure(message), id, new ChatSender(sender, ChatUtils.stringToVanilla(getName())), Instant.now()));
    }



    private int convert(ResourceKey<ChatType> type) {
        net.minecraft.core.Registry<ChatType> reg = VanillaAccess.getServer().registryHolder.registryOrThrow(Registry.CHAT_TYPE_REGISTRY);
        return reg.getId(reg.get(type));
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
        Kingship king = Crown.getKingship();
        if(king.getUniqueId() != null) return king.getUniqueId().equals(getUniqueId());
        return false;
    }

    @Override
    public void delete() {
        Crown.getUserManager().getSerializer().delete(getUniqueId());
    }

    @Nonnull
    @Override
    public Spigot spigot() {
        ensureOnline();
        return getOnlineHandle().spigot();
    }

    @Override
    public boolean isPermissionSet(@Nonnull String name) {
        ensureOnline();
        return getOnlineHandle().isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(@Nonnull Permission perm) throws UserNotOnlineException {
        ensureOnline();
        return getOnlineHandle().hasPermission(perm.getName());
    }

    @Override
    public boolean hasPermission(@Nonnull String name) throws UserNotOnlineException {
        ensureOnline();
        return getOnlineHandle().hasPermission(name);
    }

    @Override
    public boolean hasPermission(@Nonnull Permission perm) throws UserNotOnlineException {
        return hasPermission(perm.getName());
    }

    @Nonnull
    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin, @Nonnull String name, boolean value) throws UserNotOnlineException {
        ensureOnline();
        return getOnlineHandle().addAttachment(plugin, name, value);
    }

    @Nonnull
    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin) throws UserNotOnlineException {
        ensureOnline();
        return getOnlineHandle().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin, @Nonnull String name, boolean value, int ticks) throws UserNotOnlineException {
        ensureOnline();
        return getOnlineHandle().addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(@Nonnull Plugin plugin, int ticks) throws UserNotOnlineException {
        ensureOnline();
        return getOnlineHandle().addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@Nonnull PermissionAttachment attachment) throws UserNotOnlineException {
        ensureOnline();
        getOnlineHandle().removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() throws UserNotOnlineException {
        ensureOnline();
        getOnlineHandle().recalculatePermissions();
    }

    @Nonnull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() throws UserNotOnlineException {
        ensureOnline();
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

    @Override
    public void ensureOnline() throws UserNotOnlineException {
        if(!isOnline()) throw new UserNotOnlineException(this);
    }

    @Override
    public Location getLocation() {
        if(!isOnline()) return entityLocation == null ? null : entityLocation.clone();
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

        if(visitListener != null) {
            visitListener.unregister();
            hulkSmashing = true;
        }

        if(vanishTicker != null) {
            vanishTicker.stop();
            vanishTicker = null;
        }

        interactions.clearIncoming();
        interactions.clearOutgoing();
        interactions.clearInvites();
        marketData.clearIncoming();

        if(lastTeleport != null) lastTeleport.interrupt(false);

        lastLoad = System.currentTimeMillis();
        unload();
    }

    @Override
    public boolean onJoin() {
        this.handle = getOnlineHandle().getHandle();

        if(hulkSmashing) {
            RegionVisitListener listener = new RegionVisitListener(this, cosmeticData.getActiveTravel());
            listener.beginListening();
        }

        // Trigger BattlePass
        Challenges.LOG_IN.trigger(getUniqueId());

        sendPlayerListHeader(Crown.getTabList().format());

        ip = getPlayer().getAddress().getHostString();
        updateVanished();

        if(!getName().equalsIgnoreCase(lastOnlineName)){
            updateName(lastOnlineName);

            previousNames.add(lastOnlineName);
            lastOnlineName = name;
            return true;
        }
        return false;
    }

    @Override
    public void onJoinLater() {
        JoinInfo news = Crown.getJoinInfo();
        if(news.shouldShow()) sendMessage(news.display());

        updateFlying();
        afk = false;

        if(!hasPermission(Permissions.ADMIN)) setGodMode(false);
        updateGodMode();
        updateTabName();

        if(shouldResetEarnings()) resetEarnings();

        lastLoad = System.currentTimeMillis();

        //If in end, but end not open, leave end lol
        if(getWorld().equals(Worlds.end()) && !Crown.getEndOpener().isOpen()) {
            getPlayer().teleport(FtcUtils.findHazelLocation());
        }

        mail.informOfUnread();

        // Tell admin if this user has notes
        if(Punishments.hasNotes(this)) {
            StaffChat.send(
                    Component.text()
                            .color(NamedTextColor.GRAY)

                            .append(nickDisplayName().color(NamedTextColor.GOLD))
                            .append(Component.text(" has staff notes,"))
                            .append(Component.text(" [click to read]", NamedTextColor.YELLOW)
                                    .hoverEvent(Component.text("Click me :D"))
                                    .clickEvent(ClickEvent.runCommand("/notes " + getName()))
                            )

                            .build(),
                    false
            );
        }

        // Give the ark box if they've got one
        CommandArkBox.ArkBoxInfo info = CommandArkBox.ID_2_DATA.remove(getUniqueId());
        if(info != null) CommandArkBox.giveBox(this, info);
    }

    @Override
    public @NonNull HoverEvent<Component> asHoverEvent(@NonNull UnaryOperator<Component> op) {
        ProfilePrinter printer = new ProfilePrinter(this, false, false, ComponentWriter.normal());
        return HoverEvent.showText(op.apply(printer.printForHover()));
    }

    @Override
    public void updateTabName(){
        ensureOnline();

        Component displayName = listDisplayName();
        getOnlineHandle().playerListName(displayName);

        Crown.getTabList().updateList();
    }

    @Override
    public Component listDisplayName(){
        return Component.text()
                .style(FtcFormatter.nonItalic(NamedTextColor.WHITE))
                .append(getCurrentPrefix())
                .append(nickDisplayName())
                .append(isAfk() ? FtcFormatter.AFK_SUFFIX : Component.empty())
                .build();
    }

    @Override
    public Component getCurrentPrefix() {
        if(currentPrefix != null) return currentPrefix;

        if(isKing()) {
            Kingship kingship = Crown.getKingship();
            return kingship.getPrefix();
        }

        if(currentTitle != RankTitle.DEFAULT) return currentTitle.prefix();

        return Component.empty();
    }

    @Override
    public void setCurrentPrefix(Component component){
        this.currentPrefix = component;

        if(isOnline()) updateTabName();
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
    public UserTeleport getLastTeleport() {
        return lastTeleport;
    }

    @Override
    public UserTeleport createTeleport(Supplier<Location> destination, boolean tell, UserTeleport.Type type){
        return createTeleport(destination, tell, hasPermission(Permissions.TP_BYPASS), type);
    }

    @Override
    public UserTeleport createTeleport(Supplier<Location> destination, boolean tell, boolean bypassCooldown, UserTeleport.Type type){
        ensureOnline();

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
            long cooldownMillis = FtcVars.tpCooldown.get() * 50;
            nextAllowedTeleport = System.currentTimeMillis() + cooldownMillis;

            Cooldown.add(this, "Core_TeleportCooldown", FtcVars.tpCooldown.get());
        }

        lastTeleport.stop();
    }

    @Override
    public boolean checkTeleporting(){
        if(!canTeleport()){
            sendMessage(
                    Component.text("You can teleport again in ")
                            .color(NamedTextColor.GRAY)
                            .append(new TimePrinter(TimeUtil.timeUntil(nextAllowedTeleport))
                                    .print()
                                    .color(NamedTextColor.GOLD)
                            )
            );
            return false;
        }
        return true;
    }

    @Override
    public Location getLastLocation() {
        return lastLocation == null ? null : lastLocation.clone();
    }

    @Override
    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    @Override
    public CommandSource getLastMessage() {
        if(lastMessage == null) return null;
        CommandSourceStack wrapper = GrenadierUtils.senderToWrapper(lastMessage);

        return GrenadierUtils.wrap(wrapper, null);
    }

    @Override
    public void setLastMessage(CommandSource lastMessage) {
        this.lastMessage = lastMessage.asBukkit();
    }

    @Override
    public void setNickname(Component component){
        this.nickname = component;

        UserCache cache = Crown.getUserManager().getCache();
        cache.onNickChange(cache.getEntry(getUniqueId()), component == null ? null : ChatUtils.plainText(component));

        if(isOnline()) updateTabName();
    }

    @Override
    public Component nickname() {
        return nickname;
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        ensureOnline();

        Entity entity = getHandle();
        entity.setDeltaMovement(x, y, z);
        entity.hurtMarked = true;
    }

    @Override
    public void sendOrMail(Component message, @Nullable UUID sender) {
        if(isOnline()) {
            sendMessage(/*sender, */new AdventureComponent(message));
            return;
        }

        getMail().add(message, sender);
    }

    @Override
    public boolean isAfk() {
        return afk;
    }

    @Override
    public @Nullable String getAfkReason() {
        return afkReason;
    }

    @Override
    public void setAfk(boolean afk, String reason) {
        this.afk = afk;
        this.afkReason = reason;

        if(isOnline()){
            updateTabName();
            updateAfk();
        }
    }

    @Override
    public FtcGameMode getGameMode() {
        ensureOnline();
        return FtcGameMode.wrap(getPlayer().getGameMode());
    }

    @Override
    public void setGameMode(FtcGameMode gameMode) {
        ensureOnline();

        getOnlineHandle().setGameMode(gameMode.bukkit);
    }

    @Override
    public void updateVanished(){
        ensureOnline();

        if(isVanished()) {
            vanishTicker = new UserVanishTicker(this);
        } else if(vanishTicker != null) {
            vanishTicker.stop();
            vanishTicker = null;
        }

        for (CrownUser u: net.forthecrown.user.UserManager.getOnlineUsers()){
            if(u.hasPermission(Permissions.VANISH_SEE)) continue;
            if(u.equals(this)) continue;

            if(isVanished()) u.getPlayer().hidePlayer(Crown.inst(), getPlayer());
            else u.getPlayer().showPlayer(Crown.inst(), getPlayer());
        }
    }

    @Override
    public void updateAfk(){
        ensureOnline();

        if(afk){
            afkListener = new AfkListener(this);
            Bukkit.getPluginManager().registerEvents(afkListener, Crown.inst());
        } else {
            if(afkListener != null) HandlerList.unregisterAll(afkListener);

            afkReason = null;
            afkListener = null;
        }
    }

    @Override
    public void updateFlying() {
        ensureOnline();

        boolean fly = getGameMode().canFly || isFlying();
        getPlayer().setAllowFlight(fly);
    }

    @Override
    public void updateGodMode() {
        ensureOnline();

        if(godMode()){
            getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            getPlayer().setFoodLevel(20);
        }

        getPlayer().setInvulnerable(godMode());
    }

    @Override
    public void sendActionBar(@NonNull Component message) {
        ensureOnline();
        sendMessage(new AdventureComponent(message), ChatType.GAME_INFO);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NonNull Component header, @NonNull Component footer) {
        ensureOnline();
        getPlayer().sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void showTitle(net.kyori.adventure.title.Title title) {
        ensureOnline();
        getPlayer().showTitle(title);
    }

    @Override
    public void clearTitle() {
        ensureOnline();
        getPlayer().clearTitle();
    }

    @Override
    public void resetTitle() {
        ensureOnline();
        getPlayer().resetTitle();
    }

    @Override
    public void showBossBar(@NonNull BossBar bar) {
        ensureOnline();
        getPlayer().showBossBar(bar);
    }

    @Override
    public void hideBossBar(@NonNull BossBar bar) {
        ensureOnline();
        getPlayer().hideBossBar(bar);
    }

    @Override
    public void playSound(@NonNull Sound sound) {
        ensureOnline();
        getPlayer().playSound(sound);
    }

    @Override
    public void playSound(@NonNull Sound sound, double x, double y, double z) {
        ensureOnline();
        getPlayer().playSound(sound, x, y, z);
    }

    @Override
    public void stopSound(@NonNull SoundStop stop) {
        ensureOnline();
        getPlayer().stopSound(stop);
    }

    @Override
    public void openBook(@NonNull Book book) {
        ensureOnline();
        getPlayer().openBook(book);
    }

    @Override
    public void stopRiding(boolean suppressCancellation) throws UserNotOnlineException {
        ensureOnline();

        getHandle().stopRiding();
    }

    @Override
    public long getLastOnline() {
        if(lastLoad == 0) return getOfflinePlayer().getLastSeen();
        return lastLoad;
    }

    @Override
    public boolean isGoalBookDonator() {
        if(lastGuildPassDonation == 0L) return false;
        return !TimeUtil.hasCooldownEnded(FtcVars.gb_donorTimeLength.get(), getLastGuildPassDonation());
    }

    @Override
    public void setLastGuildPassDonation(long timeStamp) {
        this.lastGuildPassDonation = timeStamp;
    }

    @Override
    public long getLastGuildPassDonation() {
        return lastGuildPassDonation;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FtcUser)) return false;
        FtcUser user = (FtcUser) o;
        return user.getUniqueId().equals(getUniqueId());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(uniqueId)
                .append(getGems())
                .append(prefs)
                .append(isAfk())
                .append(getTotalEarnings())
                .append(getNextResetTime())
                .append(lastLoad)
                .append(nextAllowedTeleport)
                .toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "UUID=" + uniqueId +
                ",name='" + name + '\'' +
                '}';
    }

    protected boolean shouldResetEarnings(){
        return TimeUtil.isPast(getNextResetTime());
    }

    public void sendPacket(Packet<ClientGamePacketListener> packet){
        ensureOnline();
        Connection connection = getHandle().connection.connection;
        connection.send(packet);
    }
}