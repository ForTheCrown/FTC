package net.forthecrown.core.user;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.Permissions;
import net.forthecrown.command.Commands;
import net.forthecrown.core.CoreConfig;
import net.forthecrown.core.TabList;
import net.forthecrown.core.commands.tpa.TpMessages;
import net.forthecrown.core.commands.tpa.TpPermissions;
import net.forthecrown.core.user.UserLookupImpl.UserLookupEntry;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.text.Text;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageHandler;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.Properties;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.user.UserOfflineException;
import net.forthecrown.user.UserProperty;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.UserTeleport.Type;
import net.forthecrown.user.Users;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.DisplayIntent;
import net.forthecrown.user.name.UserNameFactory;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.Audiences;
import net.forthecrown.utils.Locations;
import net.forthecrown.utils.Time;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Internal
public final class UserImpl implements User {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private final UUID uniqueId;

  UserServiceImpl service;

  private String currentName;

  @Getter @Setter
  private String lastOnlineName;

  @Getter
  private final List<String> previousNames = new ArrayList<>();

  private Location entityLocation;
  private Location returnLocation;

  @Getter
  private boolean afk;

  @Getter
  private ViewerAwareMessage afkReason;

  @Getter @Setter
  boolean online;

  @Getter @Setter
  Player player;

  private UserComponent[] components;

  @Getter
  public UserTeleportImpl currentTeleport;

  UserVanishTicker vanishTicker;

  @Getter @Setter
  private CommandSource lastMessage;

  public UserImpl(UserServiceImpl service, UUID uniqueId) {
    Objects.requireNonNull(uniqueId);
    Objects.requireNonNull(service);

    this.uniqueId = uniqueId;
    this.service = service;

    // Determine initial 'online' state
    Player player = Bukkit.getPlayer(uniqueId);
    if (player != null) {
      online = true;
      this.player = player;
    } else {
      online = false;
    }
  }

  public void ensureValid() {
    Objects.requireNonNull(service, "User object has been invalidated :(");
  }

  @Override
  public OfflinePlayer getOfflinePlayer() {
    return Bukkit.getOfflinePlayer(getUniqueId());
  }

  @Override
  public void ensureOnline() throws UserOfflineException {
    if (isOnline()) {
      return;
    }

    throw new UserOfflineException(this);
  }

  @Override
  public String getName() {
    if (currentName == null) {
      currentName = getOfflinePlayer().getName();

      if (Strings.isNullOrEmpty(currentName)) {
        LOGGER.error("User {} has no actual player handle", getUniqueId());
        return "";
      }
    }

    return currentName;
  }

  @Override
  public PlayerProfile getProfile() {
    return getOfflinePlayer().getPlayerProfile();
  }

  @Override
  public PlayerInventory getInventory() throws UserOfflineException {
    ensureOnline();
    return getPlayer().getInventory();
  }

  @Override
  public GameMode getGameMode() throws UserOfflineException {
    ensureOnline();
    return getPlayer().getGameMode();
  }

  @Override
  public void setGameMode(GameMode gameMode) throws UserOfflineException {
    ensureOnline();
    getPlayer().setGameMode(gameMode);
  }

  @Override
  public Location getLocation() {
    if (isOnline()) {
      return getPlayer().getLocation();
    }

    return Locations.clone(entityLocation);
  }

  @Override
  public World getWorld() {
    if (isOnline()) {
      return getPlayer().getWorld();
    }

    if (entityLocation == null) {
      return null;
    }

    return entityLocation.getWorld();
  }

  @Override
  public CommandSource getCommandSource() throws UserOfflineException {
    ensureOnline();
    return Grenadier.createSource(getPlayer());
  }

  @Override
  public Location getReturnLocation() {
    return Locations.clone(returnLocation);
  }

  @Override
  public void setReturnLocation(@Nullable Location location) {
    ensureValid();
    this.returnLocation = Locations.clone(location);
  }

  public void setEntityLocation(Location entityLocation) {
    this.entityLocation = Locations.clone(entityLocation);
  }

  @Override
  public void hidePlayer(User other) throws UserOfflineException {
    ensureValid();
    this.ensureOnline();
    other.ensureOnline();

    getPlayer().hidePlayer(service.getPlugin(), other.getPlayer());
  }

  @Override
  public void showPlayer(User other) throws UserOfflineException {
    ensureValid();
    this.ensureOnline();
    other.ensureOnline();

    getPlayer().showPlayer(service.getPlugin(), other.getPlayer());
  }

  @Override
  public int getPlayTime() {
    int seconds = 0;

    if (isOnline()) {
      long loginTime = getTime(TimeField.LAST_LOGIN);
      long msSince = Time.timeSince(loginTime);
      seconds = (int) TimeUnit.MILLISECONDS.toSeconds(msSince);
    }

    return seconds + service.getPlaytime().get(uniqueId);
  }

  @Override
  public int getTotalVotes() {
    return service.getVotes().get(uniqueId);
  }

  @Override
  public void playSound(Sound sound, float volume, float pitch) {
    playSound(
        net.kyori.adventure.sound.Sound.sound()
            .type(sound)
            .volume(volume)
            .pitch(pitch)
            .build()
    );
  }

  @Override
  public <T extends UserComponent> T getComponent(Class<T> componentType) {
    ComponentFactory<T> factory = Components.getFactory(componentType);
    return getComponent(factory, factory.isRedirectAlts());
  }

  public <T extends UserComponent> T getComponent(
      ComponentFactory<T> factory,
      boolean redirectAlts
  ) {
    ensureValid();
    int id = factory.getId();

    if (service.isAltAccount(uniqueId) && redirectAlts) {
      UserImpl main = (UserImpl) Users.get(service.getMainAccount(uniqueId));
      return main.getComponent(factory, true);
    }

    if (components == null) {
      components = new UserComponent[id + 1];
      T comp = factory.newComponent(this);
      components[id] = comp;
      return comp;
    }

    if (id >= components.length) {
      components = ObjectArrays.ensureCapacity(components, id + 1);
    }

    UserComponent component = components[id];

    if (component == null) {
      component = factory.newComponent(this);
      components[id] = component;
    }

    return (T) component;
  }

  public void clearComponents() {
    Arrays.fill(components, null);
  }

  public Iterator<UserComponent> componentIterator() {
    return ArrayIterator.unmodifiable(components);
  }

  @Override
  public long getTime(TimeField field) {
    return getComponent(UserTimestamps.class).getTime(field);
  }

  @Override
  public void setTime(TimeField field, long value) {
    ensureValid();
    getComponent(UserTimestamps.class).setTime(field, value);
  }

  @Override
  public Component displayName(
      @Nullable Audience viewer,
      Set<NameRenderFlags> flags,
      DisplayIntent intent
  ) {
    Objects.requireNonNull(flags, "Null flags");
    Objects.requireNonNull(intent, "Null intent");

    ensureValid();

    NameFactoryImpl factory = service.getNameFactory();
    DisplayContext ctx = factory.createContext(this, viewer, flags).withIntent(intent);

    return factory.formatDisplayName(this, ctx);
  }

  @Override
  public Component nickOrName() {
    Component nick = nickname();
    return nick == null ? name() : nick;
  }

  @Override
  public Component name() {
    String nameStr = getName();
    return nameStr == null ? null : Component.text(nameStr);
  }

  @Override
  public Component nickname() {
    String nick = getNickname();
    return nick == null ? null : Component.text(nick);
  }

  @Override
  public String getNickOrName() {
    var nick = getNickname();
    return Strings.isNullOrEmpty(nick) ? getName() : nick;
  }

  @Override
  public String getNickname() {
    var entry = lookupEntry();
    return entry.getNickname();
  }

  @Override
  public @Nullable String getIp() {
    return lookupEntry().getIp();
  }

  @Override
  public void setNickname(String nickname) {
    ensureValid();

    UserLookupImpl lookup = service.getLookup();
    UserLookupEntry entry = lookup.getEntry(uniqueId);

    lookup.onNickChange(entry, nickname);

    if (isOnline()) {
      updateTabName();
    }
  }

  public UserLookupEntry lookupEntry() {
    ensureValid();

    UserLookupImpl lookup = service.getLookup();
    UserLookupEntry entry = lookup.getEntry(getUniqueId());

    Objects.requireNonNull(entry,
        "This user has no lookup entry? (UUID=" + getUniqueId() + ")"
    );

    return entry;
  }

  @Override
  public void setAfk(boolean afk, @Nullable ViewerAwareMessage reason) throws UserOfflineException {
    ensureOnline();
    ensureValid();

    this.afk = afk;

    if (afk) {
      this.afkReason = reason;
    } else {
      this.afkReason = null;
    }

    updateTabName();
  }

  @Override
  public long getAfkTime() {
    if (isAfk()) {
      long afkStart = getTime(TimeField.AFK_START);
      return Time.timeSince(afkStart) + getTime(TimeField.AFK_TIME);
    }

    return getTime(TimeField.AFK_TIME);
  }

  @Override
  public void afk(@Nullable ViewerAwareMessage reason)
      throws IllegalStateException, UserOfflineException
  {
    ensureOnline();
    Preconditions.checkState(!isAfk(), "User is already AFK");
    ensureValid();

    setAfk(true , reason);

    ViewerAwareMessage nonNullReason = reason == null
        ? ViewerAwareMessage.wrap(Component.empty())
        : reason;

    ChannelledMessage channelled = ChannelledMessage.create(nonNullReason)
        .setSource(this)
        .setBroadcast()
        .setChannelName("afk");

    channelled.setRenderer((viewer, baseMessage) -> {
      Component displayName;

      if (Audiences.equals(viewer, this)) {
        displayName = Component.text("You are");
      } else {
        displayName = Component.text()
            .append(displayName(viewer))
            .append(Component.text(" is"))
            .build();
      }

      Component suffix;

      if (Text.isEmpty(baseMessage)) {
        suffix = Component.text(".");
      } else {
        suffix = Component.text(": ").append(baseMessage);
      }

      return Text.format("{0} now AFK{1}", NamedTextColor.GRAY, displayName, suffix);
    });

    channelled.setHandler(MessageHandler.EMPTY_IF_VIEWER_WAS_REMOVED);
    channelled.send();
  }

  @Override
  public void unafk() throws IllegalStateException, UserOfflineException {
    ensureOnline();
    Preconditions.checkState(afk, "User is not AFK");
    ensureValid();

    setAfk(false, null);

    ChannelledMessage.announce(viewer -> {
      Component displayName;

      if (Audiences.equals(this, viewer)) {
        displayName = Component.text("You are");
      } else {
        displayName = Component.text()
            .append(displayName(viewer))
            .append(Component.text(" is"))
            .build();
      }

      return Component.text()
          .color(NamedTextColor.GRAY)
          .append(displayName)
          .append(Component.text(" no longer AFK."))
          .build();
    });
  }

  @Override
  public int getGems() {
    return service.getGems().get(uniqueId);
  }

  @Override
  public void setGems(int gems) {
    ensureValid();
    service.getGems().set(uniqueId, gems);
  }

  @Override
  public void addGems(int gems) {
    ensureValid();
    service.getGems().add(uniqueId, gems);
  }

  @Override
  public void removeGems(int gems) {
    addGems(-gems);
  }

  @Override
  public int getBalance() {
    return service.getBalances().get(uniqueId);
  }

  @Override
  public void setBalance(int balance) {
    ensureValid();
    service.getBalances().set(uniqueId, balance);
  }

  @Override
  public void addBalance(int balance) {
    ensureValid();
    service.getBalances().add(uniqueId, balance);
  }

  @Override
  public void removeBalance(int balance) {
    addBalance(-balance);
  }

  @Override
  public <T> boolean has(UserProperty<T> property) {
    return getComponent(PropertyMap.class).contains(property);
  }

  @Override
  public <T> @NotNull T get(UserProperty<T> property) {
    return getComponent(PropertyMap.class).get(property);
  }

  @Override
  public <T> void set(UserProperty<T> property, T value) {
    ensureValid();
    getComponent(PropertyMap.class).set(property, value);
  }

  private CompletableFuture<net.luckperms.api.model.user.User> getLuckPermsUser() {
    var lpManager = LuckPermsProvider.get().getUserManager();

    if (lpManager.isLoaded(uniqueId)) {
      var user = lpManager.getUser(uniqueId);
      return CompletableFuture.completedFuture(user);
    }

    return lpManager.loadUser(uniqueId);
  }

  @Override
  public boolean hasPermission(String permission) {
    if (isOnline()) {
      return getPlayer().hasPermission(permission);
    }

    var options = QueryOptions.builder(QueryMode.CONTEXTUAL)
        .flag(Flag.RESOLVE_INHERITANCE, true)
        .build();

    try {
      return getLuckPermsUser()
          .thenApply(user -> {
            return user.getCachedData()
                .getPermissionData(options)
                .checkPermission(permission)
                .asBoolean();
          })
          .get();
    } catch (ExecutionException | InterruptedException e) {
      LOGGER.error("Couldn't fetch permission data from LuckPerms", e);
    }

    return false;
  }

  @Override
  public void setPermission(String permission) {
    getLuckPermsUser().thenAccept(user -> {
      Node node = PermissionNode.builder(permission)
          .value(true)
          .build();

      user.data().add(node);
    });
  }

  @Override
  public void unsetPermission(String permission) {
    getLuckPermsUser().thenAccept(user -> {
      Node node = PermissionNode.builder(permission)
          .value(true)
          .build();

      user.data().remove(node);
    });
  }

  @Override
  public void updateVanished() {
    ensureValid();
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

  @Override
  public void updateGodMode() {
    ensureValid();
    ensureOnline();

    boolean godMode = get(Properties.GODMODE);
    var player = getPlayer();

    if (godMode) {
      var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
      assert attr != null : "Player has no Max Health attribute";

      player.setHealth(attr.getValue());
      player.setFoodLevel(20);
    }

    player.setInvulnerable(godMode);
  }

  @Override
  public void updateFlying() {
    ensureValid();
    ensureOnline();

    boolean fly = canFly(getGameMode()) || get(Properties.FLYING);
    player.setAllowFlight(fly);
  }

  private static boolean canFly(GameMode mode) {
    return switch (mode) {
      case CREATIVE, SPECTATOR -> true;
      default -> false;
    };
  }

  @Override
  public void updateTabName() {
    ensureValid();
    ensureOnline();

    Set<NameRenderFlags> flags = EnumSet.allOf(NameRenderFlags.class);
    UserNameFactory factory = service.getNameFactory();

    DisplayContext ctx = factory.createContext(this, null, flags)
        .withIntent(DisplayIntent.TABLIST);

    Component displayName = factory.formatDisplayName(this, ctx);
    player.playerListName(displayName);

    Component prefix = factory.formatPrefix(this, ctx);
    Component suffix = factory.formatSuffix(this, ctx);

    Commands.executeConsole("nte player %s clear", getName());

    if (prefix != null) {
      Commands.executeConsole("nte player %s prefix '%s &r'",
          getName(),
          Text.LEGACY.serialize(prefix)
      );
    }

    if (suffix != null) {
      Commands.executeConsole("nte player %s suffix ' %s'",
          getName(),
          Text.LEGACY.serialize(suffix)
      );
    }

    TabList.update();
  }

  @Override
  public Component checkTeleportMessage() {
    if (!canTeleport()) {
      if (isTeleporting()) {
        return TpMessages.ALREADY_TELEPORTING;
      }

      return TpMessages.canTeleportIn(getTime(TimeField.NEXT_TELEPORT));
    }

    return null;
  }

  @Override
  public boolean checkTeleporting() {
    Component component = checkTeleportMessage();
    if (component == null) {
      return true;
    }

    sendMessage(component);
    return false;
  }

  @Override
  public UserTeleport createTeleport(Supplier<Location> destination, Type type)
      throws UserOfflineException
  {
    ensureValid();

    if (currentTeleport != null) {
      currentTeleport.stop();
      currentTeleport = null;
    }

    currentTeleport = new UserTeleportImpl(this, destination, type);
    currentTeleport.setDelay(getInitialTeleportDelay());

    return currentTeleport;
  }

  private Duration getInitialTeleportDelay() {
    var perm = TpPermissions.TP_DELAY;

    if (perm.hasUnlimited(this)) {
      return null;
    }

    int seconds = perm.getTier(this).orElse(perm.getMaxTier());
    return Duration.ofSeconds(seconds);
  }

  public void onTpComplete() {
    if (currentTeleport.isDelayed()) {
      CoreConfig config = service.getConfig();
      long cooldownMillis = config.tpCooldown().toMillis();
      setTime(TimeField.NEXT_TELEPORT, System.currentTimeMillis() + cooldownMillis);
    }

    currentTeleport.stop();
    currentTeleport = null;
  }

  @Override
  public boolean isTeleporting() {
    return currentTeleport != null;
  }

  @Override
  public boolean canTeleport() {
    if (isTeleporting()) {
      return false;
    }

    return Time.isPast(getTime(TimeField.NEXT_TELEPORT));
  }

  @Override
  public Locale getLocale() {
    return isOnline() ? getPlayer().locale() : Locale.ENGLISH;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof User user)) {
      return false;
    }
    return Objects.equals(getUniqueId(), user.getUniqueId());
  }

  @Override
  public int hashCode() {
    return uniqueId.hashCode();
  }

  @Override
  public String toString() {
    return "User(id=" + getUniqueId() + ",name=" + getName() + ")";
  }
}