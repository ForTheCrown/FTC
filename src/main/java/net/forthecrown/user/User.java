package net.forthecrown.user;

import static net.forthecrown.user.data.UserTimeTracker.UNSET;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Strings;
import com.mojang.serialization.DataResult;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.AfkKicker;
import net.forthecrown.core.FTC;
import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.TabList;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.EntryNote;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.config.EndConfig;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.config.JoinInfo;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.events.dynamic.HulkSmashListener;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.data.CosmeticData;
import net.forthecrown.user.data.MailAttachment;
import net.forthecrown.user.data.MailMessage;
import net.forthecrown.user.data.RanksComponent;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserHomes;
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.user.data.UserMail;
import net.forthecrown.user.data.UserMarketData;
import net.forthecrown.user.data.UserRanks;
import net.forthecrown.user.data.UserShopData;
import net.forthecrown.user.data.UserTimeTracker;
import net.forthecrown.user.property.BoolProperty;
import net.forthecrown.user.property.Properties;
import net.forthecrown.user.property.PropertyMap;
import net.forthecrown.user.property.UserPreference;
import net.forthecrown.user.property.UserProperty;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class User implements ForwardingAudience.Single,
    HoverEventSource<Component>, Identity
{

  /**
   * The user's unique ID
   */
  @Getter
  private final UUID uniqueId;

  /**
   * The user's name
   */
  private String name;

  /**
   * The last name the user had while online
   */
  @Getter
  @Setter
  private String lastOnlineName;

  /**
   * All this users past names
   */
  @Getter
  private final ObjectList<String> previousNames = new ObjectArrayList<>();

  /**
   * The user's AFK status
   */
  @Getter
  private boolean afk = false;

  /**
   * The reason this user is AFK. Will be null if the user is not AFK, or if the user did not give a
   * reason for going AFK.
   */
  @Getter
  private Component afkReason;

  /**
   * A serialized value for ensuring users don't die if they log out during hulk smashing region
   * poles
   */
  @Getter
  public boolean hulkSmashing = false;

  /**
   * The user's IP address
   */
  @Getter
  @Setter
  private String ip;

  /**
   * The user's region visit listener, this makes sure a user will not die when impacting the ground
   * during hulk smashing and also executes travel effects
   */
  @Setter
  @Getter
  private HulkSmashListener visitListener;

  /**
   * Only used for admins that are in vanish to constantly remind them that they are, in fact, in
   * vanish and shouldn't talk in chat or give themselves away
   */
  private UserVanishTicker vanishTicker;

  /**
   * The player entity's last known location. Used for tracking the player's position
   */
  @Setter
  @Getter
  private Location entityLocation;

  /**
   * The location /back will return to for this user
   */
  @Setter
  private Location returnLocation;

  /**
   * The user's last teleport
   */
  @Getter
  UserTeleport lastTeleport;

  /**
   * The last command sender this user sent or received a direct message from
   */
  private CommandSender lastMessage;

  @Getter
  @Setter
  private UUID guildId;

  /**
   * All components attached to this user, this array may change size depending on how many
   * components have been requested of this user.
   * <p>
   * The indexes of the array correspond to {@link ComponentType#getIndex()} which are generated
   * when the component type is created.
   *
   * @see Components
   */
  private UserComponent[] components = Components.EMPTY_ARRAY;

  /* ----------------------------- CONSTRUCTOR ------------------------------ */

  User(@NotNull UUID uniqueId) {
    this.uniqueId = uniqueId;

    // Load the user's data
    reload();
  }

  /* ----------------------------- COMPONENTS ------------------------------ */

  /**
   * Gets a component by its component class. This method works by calling
   * {@link #getComponent(ComponentType)} with
   * {@link Components#of(Class, boolean)}'s result with the given class.
   *
   * @param typeClass The component to get the component of
   * @param <T>       The component's type
   * @return The component instance
   * @see #getComponent(ComponentType)
   */
  public <T extends UserComponent> @NotNull T getComponent(Class<T> typeClass) {
    return getComponent(Components.of(
        typeClass,
        typeClass == UserShopData.class
    ));
  }

  /**
   * Gets or creates a component with the given type for this user.
   * <p>
   * If the <code>type</code> says to redirect to a main account version of the
   * component, and this user has an alt, then instead of this user's instance
   * of the component being returned, the main account's instance is returned
   * instead.
   * <p>
   * Use, {@link #getComponentNoRedirect(ComponentType)} to avoid this alt/main
   * account redirection. For most cases however, there's no difference between
   * this method and the no redirect version.
   *
   * <p>
   * If this user's component array does not contain the given type, then it is
   * added to it, and potentially resized to allow for it to be added.
   *
   * @param type The type
   * @param <T>  The component's type
   * @return The gotten or created component
   */
  public <T extends UserComponent> @NotNull T getComponent(
      @NotNull ComponentType<T> type
  ) {
    Objects.requireNonNull(type);

    if (type.isRedirectAlts()) {
      var alts = UserManager.get().getAlts();
      var main = alts.getMain(getUniqueId());

      if (main != null) {
        var user = Users.get(main);
        return user.getComponent(type);
      }
    }

    return getComponentNoRedirect(type);
  }

  /**
   * Gets or creates a component with the given type for this user. If the user
   * has a main account and the given type should be redirected to the main's
   * component, no such redirect will be initiated.
   *
   * @param type Component type
   * @return Gotten or created component
   * @param <T> Component's type
   */
  @SuppressWarnings("unchecked") // The T doesn't even exist in runtime, stop screaming
  public <T extends UserComponent> @NotNull T getComponentNoRedirect(
      @NotNull ComponentType<T> type
  ) {
    Objects.requireNonNull(type);

    var index = type.getIndex();
    components = ObjectArrays.ensureCapacity(components, index + 1);

    T component = (T) components[index];

    // Component doesn't exist -> instantiate
    if (component == null) {
      return (T) (components[index] = type.create(this));
    }

    return component;
  }

  /**
   * Tests if this user has a component with the given type.
   * <p>
   * Delegate method for {@link #hasComponent(ComponentType)}
   *
   * @param type The type to test for
   * @return True, if this user has a component with the given type
   * @see #hasComponent(ComponentType)
   */
  public boolean hasComponent(Class<? extends UserComponent> type) {
    return hasComponent(Components.get(type));
  }

  /**
   * Tests if this user has a component with the given type.
   *
   * @param type The type to test for
   * @return True, if this user has a component with the given type
   */
  public boolean hasComponent(ComponentType<?> type) {
    if (type == null) {
      return false;
    }

    if (type.getIndex() >= components.length) {
      return false;
    }

    return components[type.getIndex()] != null;
  }

  /**
   * Creates an iterator which loops through all components this user has.
   *
   * @return The user's component iterator
   */
  public Iterator<UserComponent> componentIterator() {
    // No components, empty iterator
    if (!hasComponents()) {
      return Collections.emptyListIterator();
    }

    return ArrayIterator.unmodifiable(components);
  }

  /**
   * Tests if the user has any components
   *
   * @return True, if the user has any non-null components, false otherwise
   */
  public boolean hasComponents() {
    if (components.length == 0) {
      return false;
    }

    return ArrayIterator.unmodifiable(components)
        .hasNext();
  }

  /**
   * Clears all the user's components by setting {@link #components} equal to
   * {@link Components#EMPTY_ARRAY}
   */
  public void clearComponents() {
    components = Components.EMPTY_ARRAY;
  }

  /* ----------------------------- COMPONENT DELEGATE METHODS ------------------------------ */

  /**
   * Delegate method for getting {@link Components#COSMETICS}
   *
   * @see #getComponent(ComponentType)
   * @see Components#COSMETICS
   */
  public CosmeticData getCosmeticData() {
    return getComponent(Components.COSMETICS);
  }

  /**
   * Delegate method for getting {@link Components#PROPERTIES}
   *
   * @see #getComponent(ComponentType)
   * @see Components#PROPERTIES
   */
  public PropertyMap getProperties() {
    return getComponent(Components.PROPERTIES);
  }

  /**
   * Delegate method for getting {@link Components#HOMES}
   *
   * @see #getComponent(ComponentType)
   * @see Components#HOMES
   */
  public UserHomes getHomes() {
    return getComponent(Components.HOMES);
  }

  /**
   * Delegate method for getting {@link Components#MAIL}
   *
   * @see #getComponent(ComponentType)
   * @see Components#MAIL
   */
  public UserMail getMail() {
    return getComponent(Components.MAIL);
  }

  /**
   * Delegate method for getting {@link Components#MARKET_DATA}
   *
   * @see #getComponent(ComponentType)
   * @see Components#MARKET_DATA
   */
  public UserMarketData getMarketData() {
    return getComponent(Components.MARKET_DATA);
  }

  /**
   * Delegate method for getting {@link Components#TIME_TRACKER}
   *
   * @see #getComponent(ComponentType)
   * @see Components#TIME_TRACKER
   */
  public UserTimeTracker getTimeTracker() {
    return getComponent(Components.TIME_TRACKER);
  }

  /**
   * Delegate method for getting {@link Components#INTERACTIONS}
   *
   * @see #getComponent(ComponentType)
   * @see Components#INTERACTIONS
   */
  public UserInteractions getInteractions() {
    return getComponent(Components.INTERACTIONS);
  }

  /**
   * Delegate method for getting {@link Components#TITLES}
   *
   * @see #getComponent(ComponentType)
   * @see Components#TITLES
   */
  public RanksComponent getTitles() {
    return getComponent(Components.TITLES);
  }


  /* ----------------------------- METHODS ------------------------------ */

  /**
   * Gets the user's Bukkit {@link Player} object
   *
   * @return The user's player object, null, if not online
   */
  public Player getPlayer() {
    return Bukkit.getPlayer(getUniqueId());
  }

  /**
   * Gets the user's {@link OfflinePlayer} object.
   *
   * @return The user's offline player object
   */
  public OfflinePlayer getOfflinePlayer() {
    return Bukkit.getOfflinePlayer(getUniqueId());
  }

  /**
   * Tests if the user is online
   *
   * @return True, if {@link #getPlayer()} != null
   */
  public boolean isOnline() {
    return getPlayer() != null;
  }

  /**
   * Code to run when the player joins
   *
   * @return True, if the player changed their name since the last time they logged on
   */
  public boolean onJoin() {
    UserManager.get().getOnline().put(getUniqueId(), this);

    // If player logged out while hulk smash landing
    if (hulkSmashing) {
      this.visitListener = new HulkSmashListener(this, getCosmeticData().get(Cosmetics.TRAVEL));
      visitListener.beginListening();
    }

    // Update IP
    var socketAddress = getPlayer().getAddress();
    if (socketAddress != null) {
      ip = socketAddress.getHostString();
    }

    // Show join info, if we should
    var joinInfo = JoinInfo.display();
    if (!Component.empty().equals(joinInfo)) {
      sendMessage(joinInfo);
    }

    // We are definitely not AFK lol
    afk = false;

    // Update all the properties
    updateFlying();
    updateVanished();
    updateGodMode();
    updateTabName();

    // If in end, but end not open, leave end lol
    if (getWorld().equals(Worlds.end())
        && !EndConfig.open
        && !hasPermission(Permissions.ADMIN)
    ) {
      getPlayer().teleport(GeneralConfig.getServerSpawn());
    }

    if (hasComponent(Components.MAIL)) {
      getMail().informOfUnread();
    }

    if (hasComponent(UserShopData.class)) {
      getComponent(UserShopData.class).onLogin(getTimeTracker());
    }

    getTimeTracker().setCurrent(TimeField.LAST_LOGIN);

    // Inform of guild Exp Multiplier
    var modifier = GuildManager.get()
        .getExpModifier()
        .getModifier(getUniqueId());

    if (getGuild() != null && modifier > 1) {
      Tasks.runLaterAsync(
          () -> sendMessage(Messages.guildMultiplierActive(modifier)),
          60
      );
    }

    // Tell admin if this user has notes
    if (Punishments.hasNotes(this)) {
      var writer = TextWriters.newWriter();
      var notes = Punishments.entry(this).getNotes();

      EntryNote.writeNotes(notes, writer, this);

      Users.getOnline()
          .stream()
          .filter(user -> {
            if (!user.hasPermission(Permissions.PUNISH_NOTES)) {
              return false;
            }

            return user.get(Properties.VIEWS_NOTES)
                && !user.getUniqueId().equals(this.getUniqueId());
          })

          .forEach(user -> user.sendMessage(writer.asComponent()));
    }

    // Ensure that titles are synced to permissions
    getTitles().ensureSynced();

    // Ensure names and such are in sync
    if (Strings.isNullOrEmpty(lastOnlineName)) {
      lastOnlineName = getName();
    }

    // If name has been changed, transfer across scores
    // and update lastOnlineName variable
    if (!Objects.equals(getName(), lastOnlineName)) {
      updateName(lastOnlineName);

      previousNames.add(lastOnlineName);
      lastOnlineName = name;

      return true;
    }

    return false;
  }

  /**
   * Executed when the player leaves the server.
   * <p>
   * This turns off all listeners linked to this user, logs the user's play time and sets the
   * {@link #entityLocation} to the player's location
   */
  public void onLeave() {
    entityLocation = getPlayer().getLocation();

    if (visitListener != null) {
      visitListener.unregister();
      hulkSmashing = true;
    }

    if (vanishTicker != null) {
      vanishTicker.stop();
      vanishTicker = null;
    }

    var interactions = getInteractions();
    interactions.clearIncoming();
    interactions.clearOutgoing();
    getMarketData().clearIncoming();

    if (lastTeleport != null) {
      lastTeleport.interrupt();
    }

    // Log play time
    logTime().resultOrPartial(Loggers.getLogger()::warn).ifPresent(integer -> {
      Loggers.getLogger().info("Adding {} seconds or {} hours to {}'s playtime",
          integer,
          TimeUnit.SECONDS.toHours(integer),
          this
      );

      UserManager.get().getPlayTime()
          .add(getUniqueId(), integer);
    });

    lastOnlineName = getName();

    // Make sure the user is not marriage chat
    if (interactions.isMarried()) {
      set(Properties.MARRIAGE_CHAT, false);
    }

    UserManager.get().getOnline().remove(getUniqueId());

    TabList.update();
  }

  /**
   * Logs the player's playtime
   */
  private DataResult<Integer> logTime() {
    var join = getTimeTracker().get(TimeField.LAST_LOGIN);

    if (join == UNSET) {
      return Results.error("Join time not set");
    }

    var played = Time.timeSince(join) - getAfkTime();
    var timeSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(played);

    return Results.success(timeSeconds);
  }

  /* ----------------------------- MAIL ------------------------------ */

  /**
   * Sends a mail message to this user. If the user is online, they will be told they got a mail
   * message.
   *
   * @param message The message to send
   */
  public void sendMail(MailMessage message) {
    getMail().add(message);

    // User is not online, don't attempt to tell
    // them about the message they received
    if (!isOnline()) {
      return;
    }

    Component text = message.getMessage();
    User senderUser = null;

    // If message has a sender, get them
    if (message.getSender() != null) {
      UUID sender = message.getSender();
      senderUser = Users.get(sender);
      senderUser.unloadIfOffline();
    }

    sendMessage(
        Messages.mailReceived(
            text, senderUser,
            !MailAttachment.isEmpty(message.getAttachment())
        )
    );
  }

  /**
   * Sends a message to this user or sends a mail message. This works by just checking if the user
   * is online or not, if they are, it sends a message, if not, it adds a message to their mail.
   *
   * @param c The message to send
   */
  public void sendOrMail(Component c) {
    if (isOnline()) {
      sendMessage(c);
      return;
    }

    getMail().add(c);
  }

  /* ----------------------------- UTILITY ------------------------------ */

  /**
   * Ensures the user is online.
   * <p>
   * If the user is not online, this method will throw a {@link UserOfflineException} to prevent any
   * online-only code from being executed.
   *
   * @throws UserOfflineException If the user is not online
   */
  public void ensureOnline() throws UserOfflineException {
    if (!isOnline()) {
      throw new UserOfflineException(this);
    }
  }

  /**
   * Reloads the user's data and sets the {@link TimeField#LAST_LOADED} to the current time.
   */
  public void reload() {
    UserManager.get()
        .getSerializer()
        .deserialize(this);
  }

  /**
   * Saves the user's data and sets the {@link TimeField#LAST_LOADED} to the current time
   */
  public void save() {
    UserManager.get()
        .getSerializer()
        .serialize(this);
  }

  /**
   * Gets the user's name
   * <p>
   * If the {@link #name} field is null, this method sets it to be equal to
   * {@link #getOfflinePlayer()}'s name
   *
   * @return The user's name
   */
  public String getName() {
    return name == null ? name = getOfflinePlayer().getName() : name;
  }

  /**
   * Update's the user's names. This will transfer over all the user's scores on the scoreboard from
   * their last name to the new one.
   *
   * @param last The user's last name
   */
  public void updateName(String last) {
    // Ensure we're online and that the last name is not null
    if (!isOnline()
        || last == null
        || last.equals(getPlayer().getName())
    ) {
      return;
    }

    // Update the user cache with a new name
    UserLookup cache = UserManager.get().getUserLookup();
    String newName = getPlayer().getName();
    Scoreboard scoreboard = getPlayer().getScoreboard();

    cache.onNameChange(cache.getEntry(getUniqueId()), newName);

    // Transfer scores
    for (Objective obj : scoreboard.getObjectives()) {
      if (!obj.getScore(last).isScoreSet()) {
        continue;
      }

      var lastScore = obj.getScore(last);
      int score = lastScore.getScore();
      lastScore.resetScore();

      obj.getScore(newName)
          .setScore(score);
    }
  }

  /**
   * Unloads the user if they're not online
   */
  public void unloadIfOffline() {
    if (isOnline()) {
      return;
    }

    UserManager.get().unload(this);
  }

  /**
   * Deletes this user's data
   */
  public void delete() {
    UserManager.get().getSerializer().delete(getUniqueId());
  }

  /**
   * Gets the profile of this user
   * <p>
   * Note: It's just a new craft player profile that has no properties or anything. Do
   * {@link PlayerProfile#complete()} for those
   *
   * @return The user's profile
   */
  public CraftPlayerProfile getProfile() {
    CraftPlayerProfile profile = new CraftPlayerProfile(getUniqueId(), getName());
    profile.completeFromCache(false, true);

    return profile;
  }

  /**
   * Gets the user's location. If the user is offline {@link #entityLocation} will be returned
   * instead.
   *
   * @return The user's location, if offline, then it's the user's last known location.
   */
  public Location getLocation() {
    if (!isOnline()) {
      return entityLocation == null ? null : entityLocation.clone();
    }

    return (entityLocation = getPlayer().getLocation()).clone();
  }

  /**
   * Gets the world the user is currently in.
   * <p>
   * If the user is offline, returns {@link #entityLocation}'s world, which may be null.
   *
   * @return The user's world
   */
  public World getWorld() {
    if (!isOnline()) {
      // We don't know the entity's
      // location, return null
      if (entityLocation == null) {
        return null;
      }

      return entityLocation.getWorld();
    }

    return getLocation().getWorld();
  }

  /**
   * Gets the command source object for this user
   *
   * @return The {@link CommandSource} that represents the player of this user object
   * @throws UserOfflineException If the user is not online
   */
  public CommandSource getCommandSource() throws UserOfflineException {
    ensureOnline();
    return Grenadier.createSource(getPlayer());
  }

  /**
   * Gets the user's return location. This is the location the user should return to when they use
   * /back. As such, it should be updated whenever the user TPAs, goes home or uses a region pole
   *
   * @return The user's return location
   */
  public Location getReturnLocation() {
    return returnLocation == null ? null : returnLocation.clone();
  }

  /**
   * Gets the last message recipient the user spoke to
   *
   * @return The last message recipient the user spoke to, null, if the user is offline or has no
   * message recipients.
   */
  public CommandSource getLastMessage() {
    // No reply target?
    if (lastMessage == null) {
      return null;
    }

    return Grenadier.createSource(lastMessage);
  }

  /**
   * Sets the last message recipient of this user
   * <p>
   * Because the last message recipient is actually stored as a {@link CommandSender}, this method
   * will call {@link CommandSource#asBukkit()} to set the field's value.
   *
   * @param lastMessage The user's last message recipient.
   */
  public void setLastMessage(CommandSource lastMessage) {
    this.lastMessage = lastMessage.asBukkit();
  }


  /**
   * Plays a message to this user only
   *
   * @param sound  The sound to play
   * @param volume The volume of the sound
   * @param pitch  The pitch of the sound
   */
  public void playSound(Sound sound, float volume, float pitch) {
    if (isOnline()) {
      getPlayer().playSound(getLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }
  }

  /* ----------------------------- ECONOMY ------------------------------ */

  /**
   * Adds gems to this user
   *
   * @param gems The amount of gems to add
   */
  public void addGems(int gems) {
    setGems(getGems() + gems);
  }

  /**
   * Sets the user's gem amount
   *
   * @param gems the amount of gems this user will have
   */
  public void setGems(int gems) {
    UserManager.get().getGems().set(getUniqueId(), gems);
  }

  /**
   * Gets the amount of gems this user has
   *
   * @return The user's gem count
   */
  public int getGems() {
    return UserManager.get().getGems().get(getUniqueId());
  }

  public int getBalance() {
    return UserManager.get().getBalances().get(getUniqueId());
  }

  public void setBalance(int balance) {
    UserManager.get().getBalances().set(getUniqueId(), balance);
  }

  public void addBalance(int amount) {
    UserManager.get().getBalances().add(getUniqueId(), amount);
  }

  public void removeBalance(int amount) {
    UserManager.get().getBalances().remove(getUniqueId(), amount);
  }

  public boolean hasBalance(int amount) {
    return getBalance() >= amount;
  }

  /* ----------------------------- PERMISSIONS ------------------------------ */

  /**
   * Tests if this user has the given permission
   *
   * @param name The name of the permission to test
   * @return True, if the user has the permission, false otherwise
   */
  public boolean hasPermission(@Nonnull String name) {
    if (isOnline()) {
      return getPlayer().hasPermission(name);
    }

    // OPs have all permissions
    if (getOfflinePlayer().isOp()) {
      return true;
    }

    var lpManager = LuckPermsProvider.get().getUserManager();

    var options = QueryOptions.builder(QueryMode.NON_CONTEXTUAL)
        .flag(Flag.RESOLVE_INHERITANCE, true)
        .build();

    if (lpManager.isLoaded(getUniqueId())) {
      var cachedData = lpManager.getUser(getUniqueId());
      assert cachedData != null : "User has no LP cache data";

      return cachedData.getCachedData()
          .getPermissionData(options)
          .checkPermission(name)
          .asBoolean();
    }

    try {
      return lpManager.loadUser(getUniqueId())
          .get()
          .getCachedData()
          .getPermissionData(options)
          .checkPermission(name)
          .asBoolean();
    } catch (ExecutionException | InterruptedException e) {
      Loggers.getLogger().error("Couldn't fetch permission data from LuckPerms", e);
    }

    return false;
  }

  /**
   * Tests if the user has the given permission
   *
   * @param perm The permission to test
   * @return True, if the user has the permission, false otherwise
   */
  public boolean hasPermission(@Nonnull Permission perm) {
    return hasPermission(perm.getName());
  }

  /* ----------------------------- TELEPORTATION ------------------------------ */

  /**
   * Tests if a user can teleport. This function will also tell the user when they can teleport
   * again.
   *
   * @return True, if the user can teleport, false otherwise
   */
  public boolean checkTeleporting() {
    if (!canTeleport()) {
      if (isTeleporting()) {
        sendMessage(Messages.ALREADY_TELEPORTING);
        return false;
      }

      sendMessage(Messages.canTeleportIn(getTime(TimeField.NEXT_TELEPORT)));
      return false;
    }

    return true;
  }

  /**
   * Creates a {@link UserTeleport} to the given destination.
   * <p>
   * Note: The teleport itself won't start until {@link UserTeleport#start()} is called.
   *
   * @param destination The destination supplier, see: {@link UserTeleport#getDestination()}
   * @param type        The teleprot type
   * @return The created teleport
   * @throws UserOfflineException If the user isn't online
   */
  public UserTeleport createTeleport(Supplier<Location> destination, UserTeleport.Type type)
      throws UserOfflineException {
    ensureOnline();

    // Last teleport is not null, stop it
    if (lastTeleport != null) {
      lastTeleport.stop();
    }

    // Create teleport and return it
    return lastTeleport = new UserTeleport(this, destination, type)
        .setDelayed(!Permissions.TP_DELAY.hasUnlimited(this));
  }

  /**
   * Callback function for {@link UserTeleport} to call when its teleportation finishes.
   * <p>
   * If the last teleport had a delay, it gives this user a cooldown until it can teleport again
   */
  void onTpComplete() {
    if (lastTeleport.isDelayed()) {
      long cooldownMillis = GeneralConfig.tpCooldown * 50L;
      setTime(TimeField.NEXT_TELEPORT, System.currentTimeMillis() + cooldownMillis);
    }

    // No need to set the teleport
    // to null here, the stop() method
    // does that for us
    lastTeleport.stop();
  }

  /**
   * Tests if the user has a currently active teleport.
   *
   * @return True, if the user has an active teleport
   */
  public boolean isTeleporting() {
    return lastTeleport != null;
  }

  /**
   * Tests if the user can teleport. If {@link #isTeleporting()} returns true, this method will
   * return false. Otherwise, this method will return if the {@link TimeField#NEXT_TELEPORT} is in
   * the past or is unset.
   *
   * @return True if the user can teleport, false otherwise
   */
  public boolean canTeleport() {
    if (isTeleporting()) {
      return false;
    }

    return Time.isPast(getTime(TimeField.NEXT_TELEPORT));
  }

  /* ----------------------------- DISPLAY METHODS ------------------------------ */

  @Override
  public @NonNull
  HoverEvent<Component> asHoverEvent(@NonNull UnaryOperator<Component> op) {
    UserFormat format = UserFormat.create(this)
        .with(UserFormat.FOR_HOVER)
        .disableHover();

    TextWriter writer = TextWriters.newWriter();

    UserFormat.applyProfileStyle(writer);
    format.format(writer);

    return HoverEvent.showText(op.apply(writer.asComponent()));
  }

  /**
   * Gets the user's click event, '/tell [name] ' if online, '/profile [name]' if offline
   *
   * @return The user's click event
   */
  public ClickEvent getClickEvent() {
    return getClickEvent(isOnline());
  }

  /**
   * Gets the user's click event.
   *
   * @param online Whether to return the online click event or offline click event. If true, returns
   *               the '/tell', otherwise '/profile'
   * @return The created click event
   */
  public ClickEvent getClickEvent(boolean online) {
    return ClickEvent.suggestCommand(
        "/" + (online ? "tell " + getName() + " " : "profile " + getName()));
  }

  /**
   * Gets the user's display name, with the nickname, if they have it
   *
   * @return The user's display name, possibly with a nickname
   */
  public Component displayName() {
    return displayName(isOnline());
  }

  public Component displayName(boolean online) {
    return getTabName()
        .insertion(getUniqueId().toString())
        .clickEvent(getClickEvent(online))
        .hoverEvent(this);
  }

  /**
   * Whether the user has a nickname or not
   *
   * @return True, if the user has a nickname
   */
  public boolean hasNickname() {
    return getNickname() != null;
  }

  /**
   * Gets either the name of the user, or their nickname
   *
   * @return The user's nickname or name
   */
  public String getNickOrName() {
    return hasNickname() ? getNickname() : getName();
  }

  public String getNickname() {
    return UserManager.get()
        .getUserLookup()
        .getEntry(getUniqueId())
        .getNickname();
  }

  /**
   * Same as {@link #getNickOrName()} except component
   *
   * @return The user's nick or name
   */
  public Component nickOrName() {
    return Component.text(getNickOrName());
  }

  /**
   * Gets the user's name in Component format
   *
   * @return The user's name in component format.
   */
  public Component name() {
    return Component.text(getName());
  }

  /**
   * Sets the user's nickname and updates the {@link UserLookup}
   *
   * @param component The new nickname
   */
  public void setNickname(String component) {
    // Update the user cache
    UserLookup cache = UserManager.get().getUserLookup();
    cache.onNickChange(cache.getEntry(getUniqueId()), component);

    // If online, update tab name
    if (isOnline()) {
      updateTabName();
    }
  }

  /**
   * Gets the user's effective prefix.
   *
   * @return The user's effective prefix
   */
  public @NotNull Component getEffectivePrefix(boolean allowRank) {
    // If there's a set prefix, return it
    if (getProperties().contains(Properties.PREFIX)) {
      return get(Properties.PREFIX);
    }

    // If our title is anything other han default,
    // return it
    if (allowRank && getTitles().getTitle() != UserRanks.DEFAULT) {
      return getTitles().getTitle().getPrefix();
    }

    // Otherwise return an empty prefix
    return Component.empty();
  }

  /**
   * Updates the user's tab display name
   *
   * @throws UserOfflineException If the user is offline
   */
  public void updateTabName() throws UserOfflineException {
    ensureOnline();

    Component displayName = listDisplayName(true, true);
    getPlayer().playerListName(displayName);

    // Mmm yes, let's put our API on an insecure HTTP protocol instead
    // of an HTTPS protocol
    Component prefix = getEffectivePrefix(true);
    Component suffix = get(Properties.SUFFIX);

    Util.consoleCommand("nte player %s clear", getName());

    if (!prefix.equals(Component.empty())) {
      Util.consoleCommand("nte player %s prefix '%s &r'",
          getName(),
          Text.LEGACY.serialize(prefix)
      );
    }

    if (!suffix.equals(Component.empty())) {
      Util.consoleCommand("nte player %s suffix ' %s'",
          getName(),
          Text.LEGACY.serialize(suffix)
      );
    }

    TabList.update();
  }

  /**
   * Gets the component to display in the tab menu for this user.
   *
   * @return The user's TAB display name
   */
  public Component listDisplayName(boolean prependRank, boolean allowAfk) {
    return Users.createListName(this, getTabName(), prependRank, allowAfk);
  }

  public Component getTabName() {
    if (getProperties().contains(Properties.TAB_NAME)) {
      return get(Properties.TAB_NAME);
    }

    return nickOrName();
  }

  /* ----------------------------- DELEGATE METHODS ------------------------------ */

  /**
   * Delegate method for {@link PropertyMap#get(UserProperty)}
   *
   * @see PropertyMap#get(UserProperty)
   */
  public <T> T get(UserProperty<T> property) {
    return getProperties().get(property);
  }

  /**
   * Delegate method for {@link PropertyMap#set(UserProperty, Object)}
   *
   * @see PropertyMap#set(UserProperty, Object)
   */
  public <T> void set(UserProperty<T> property, T value) {
    getProperties().set(property, value);
  }

  /**
   * Delegate method for {@link PropertyMap#flip(BoolProperty)}
   *
   * @see PropertyMap#flip(BoolProperty)
   */
  public void flip(BoolProperty property) {
    getProperties().flip(property);
  }

  /**
   * Gets the message audience of this user. If the user is offline, this returns
   * {@link Audience#empty()}, otherwise, it returns {@link #getPlayer()}
   *
   * @return The audience that represents this user
   */
  @Override
  public @NotNull Audience audience() {
    return isOnline() ? getPlayer() : Audience.empty();
  }

  /**
   * Gets the inventory of the player this user represents
   *
   * @return The user's inventory
   * @throws UserOfflineException If the user is offline
   */
  public PlayerInventory getInventory() throws UserOfflineException {
    ensureOnline();
    return getPlayer().getInventory();
  }

  /**
   * Updates the user's vanished state. If the user is vanished it removes them
   * from the view of any other players that cannot see vanished players.
   *
   * @throws UserOfflineException If the user is offline
   */
  public void updateVanished() throws UserOfflineException {
    ensureOnline();
    boolean vanished = get(Properties.VANISHED);

    // Make sure vanish ticker is active
    if (vanished) {
      if (vanishTicker == null) {
        vanishTicker = new UserVanishTicker(this);
      }
    } else if (vanishTicker != null) {
      vanishTicker.stop();
      vanishTicker = null;
    }

    boolean canSeeVanished = hasPermission(Permissions.VANISH_SEE);

    if (vanished) {
      UserPreference.DYNMAP_HIDE.setState(this, true);
    }

    // Go through all online users to hide this
    // user from that online user
    for (var u : Users.getOnline()) {
      // If the user is this, ignore
      if (u.equals(this)) {
        continue;
      }

      boolean otherVanished = u.get(Properties.VANISHED);

      // Update our perspective of the other user
      if (!canSeeVanished) {
        if (otherVanished) {
          hidePlayer(u);
        } else {
          showPlayer(u);
        }
      } else {
        showPlayer(u);
      }

      // Update other user's perspective of us
      if (!u.hasPermission(Permissions.VANISH_SEE)) {
        if (vanished) {
          u.hidePlayer(this);
        } else {
          u.showPlayer(this);
        }
      } else {
        u.showPlayer(this);
      }
    }
  }

  /**
   * Shows the specified {@code other} user to this user. Only has an effect if
   * the {@code other} has been hidden prior to this method call.
   *
   * @param other user to show (un-hide) to this user
   * @throws UserOfflineException If either this user or {@code other} is
   *                              offline
   */
  public void showPlayer(User other) throws UserOfflineException {
    this.ensureOnline();
    other.ensureOnline();

    getPlayer().showPlayer(FTC.getPlugin(), other.getPlayer());
  }

  /**
   * Hides the specified {@code other} user from this user.
   * @param other user to hide from this user
   * @throws UserOfflineException If either this user or {@code other} is
   *                              offline
   */
  public void hidePlayer(User other) throws UserOfflineException {
    this.ensureOnline();
    other.ensureOnline();

    getPlayer().hidePlayer(FTC.getPlugin(), other.getPlayer());
  }

  /**
   * Updates the user's godmode status. Just sets {@link Player#setInvulnerable(boolean)} to true
   * for this player's user
   *
   * @throws UserOfflineException If the user is offline
   */
  public void updateGodMode() throws UserOfflineException {
    ensureOnline();

    boolean godMode = get(Properties.GOD);
    var player = getPlayer();

    if (godMode) {
      var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
      assert attr != null : "Player has no Max Health attribute";

      player.setHealth(attr.getValue());
      player.setFoodLevel(20);
    }

    player.setInvulnerable(godMode);
  }

  /**
   * Calls {@link Player#setAllowFlight(boolean)} for this user with the input depending on if the
   * user's game mode allows flying or if {@link Properties#FLYING} is set to true for this user.
   *
   * @throws UserOfflineException If the user is offline
   */
  public void updateFlying() throws UserOfflineException {
    ensureOnline();

    boolean fly = canFly(getGameMode()) || get(Properties.FLYING);
    getPlayer().setAllowFlight(fly);
  }

  public GameMode getGameMode() throws UserOfflineException {
    ensureOnline();
    return getPlayer().getGameMode();
  }

  public void setGameMode(GameMode newGameMode) throws UserOfflineException {
    ensureOnline();

    var currentGameMode = getGameMode();

    if (currentGameMode == GameMode.SPECTATOR
        && newGameMode != GameMode.SPECTATOR
    ) {
      boolean hide = get(Properties.DYNMAP_HIDE);
      UserPreference.DYNMAP_HIDE.setState(this, hide);
    }

    getPlayer().setGameMode(newGameMode);
    updateFlying();

    if (newGameMode == GameMode.SPECTATOR
        && currentGameMode != GameMode.SPECTATOR
    ) {
      boolean hide = UserPreference.DYNMAP_HIDE.getState(this);
      set(Properties.DYNMAP_HIDE, hide);

      UserPreference.DYNMAP_HIDE.setState(this, true);
    }
  }

  private static boolean canFly(GameMode mode) {
    return switch (mode) {
      case CREATIVE, SPECTATOR -> true;
      default -> false;
    };
  }

  @Override
  public java.util.@NotNull UUID uuid() {
    return getUniqueId();
  }

  public long getLastLogin() {
    if (!getTimeTracker().isSet(TimeField.LAST_LOGIN)) {
      return getOfflinePlayer().getLastLogin();
    }

    return getTimeTracker().get(TimeField.LAST_LOGIN);
  }

  public long getTime(TimeField field) {
    return getTimeTracker().get(field);
  }

  public void setTime(TimeField field, long time) {
    getTimeTracker().set(field, time);
  }

  public void setTimeToNow(TimeField field) {
    getTimeTracker().setCurrent(field);
  }

  /* ----------------------------- AFK ------------------------------ */

  public void setAfk(boolean afk, Component reason) throws UserOfflineException {
    ensureOnline();

    this.afk = afk;
    this.afkReason = reason;

    updateTabName();
    updateAfk();
  }

  public void updateAfk() throws UserOfflineException {
    ensureOnline();

    if (afk) {
      getTimeTracker().setCurrent(TimeField.AFK_START);
    } else {
      afkReason = null;

      var started = getTimeTracker().get(TimeField.AFK_START);
      var passed = Time.timeSince(started);

      getTimeTracker().add(TimeField.AFK_TIME, passed);
    }
  }

  public long getAfkTime() {
    if (isAfk()) {
      return Time.timeSince(getTime(TimeField.AFK_START));
    }

    return getTimeTracker().isSet(TimeField.AFK_TIME) ? getTime(TimeField.AFK_TIME) : 0L;
  }

  /**
   * Makes this user enter an AFK state
   *
   * @param reason The reason the user is entering the AFK state
   * @throws IllegalArgumentException If the user is already AFK
   * @throws UserOfflineException     If the user is not AFK
   */
  public void afk(@Nullable Component reason)
      throws IllegalArgumentException, UserOfflineException
  {
    ensureOnline();
    Validate.isTrue(!afk, "User is already AFK");

    setAfk(true, reason);

    var mute = Punishments.muteStatus(this);
    boolean containsBannedWords
        = reason != null && BannedWords.checkAndWarn(getPlayer(), reason);

    boolean showMessageSelf
        = containsBannedWords || !mute.isVisibleToSender();

    boolean showMessageOthers
        = containsBannedWords || !mute.isVisibleToOthers();

    Component selfReason = showMessageSelf ? null : reason;
    Component otherReason = showMessageOthers ? null : reason;

    Users.getOnline()
        .stream()
        .filter(user -> !user.getUniqueId().equals(getUniqueId()))
        .forEach(user -> {
          var showReason = otherReason;

          if (Users.areBlocked(this, user)) {
            showReason = null;
          }

          user.sendMessage(Messages.afkOthers(this, showReason));
        });

    AfkKicker.onAfk(getUniqueId());
    sendMessage(Messages.afkSelf(selfReason));
  }

  /**
   * Makes this user leave their AFK state
   *
   * @throws IllegalArgumentException If the user is not AFK
   * @throws UserOfflineException     If the user is not online
   */
  public void unafk() throws IllegalArgumentException, UserOfflineException {
    ensureOnline();
    Validate.isTrue(afk, "User is not AFK");

    setAfk(false, null);

    var announcement = Messages.unafk(this);

    Users.getOnline()
        .stream()
        .filter(user -> !user.getUniqueId().equals(getUniqueId()))
        .forEach(user -> user.sendMessage(announcement));

    sendMessage(Messages.UN_AFK_SELF);
  }

  /* ----------------------------- GUILDS ------------------------------ */

  public void setGuild(Guild guild) {
    setGuildId(guild == null ? null : guild.getId());
  }

  public Guild getGuild() {
    return getGuildId() == null ? null : GuildManager.get().getGuild(getGuildId());
  }

  public String getDiscordId() {
    if (!FtcDiscord.isActive()) {
      return null;
    }

    return DiscordSRV.getPlugin()
        .getAccountLinkManager()
        .getDiscordId(getUniqueId());
  }

  public Optional<Member> getDiscordMember() {
    var discId = getDiscordId();

    if (Strings.isNullOrEmpty(discId)) {
      return Optional.empty();
    }

    return Optional.ofNullable(
        DiscordSRV.getPlugin()
            .getMainGuild()
            .getMemberById(discId)
    );
  }

  /* ----------------------------- OBJECT OVERRIDES ------------------------------ */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof User user)) {
      return false;
    }

    return user.getUniqueId().equals(getUniqueId());
  }

  @Override
  public int hashCode() {
    return getUniqueId().hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s:{UUID=%s, name='%s'}",
        getClass().getSimpleName(),
        getUniqueId(),
        getName()
    );
  }
}