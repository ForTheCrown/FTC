package net.forthecrown.core.user;

import static net.forthecrown.user.UserNameFactory.ALLOW_NICKNAME;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.core.user.UserLookupImpl.UserLookupEntry;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.user.UserNameFactory;
import net.forthecrown.user.UserOfflineException;
import net.forthecrown.user.UserProperty;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.UserTeleport.Type;
import net.forthecrown.user.Users;
import net.forthecrown.user.event.UserAfkEvent;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.Locations;
import net.forthecrown.utils.Time;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Internal
final class UserImpl implements User {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private final UUID uniqueId;

  UserServiceImpl service;

  private String currentName;

  @Getter @Setter
  private String lastOnlineName;

  @Getter @Setter
  private String ip;

  @Getter
  private final List<String> previousNames = new ArrayList<>();

  @Setter
  private Location entityLocation;
  private Location returnLocation;

  // Lazily initialized in setTime(TimeField, long)
  @Getter @Setter
  private long[] timeFields;

  @Getter
  private boolean afk;

  @Getter
  private Component afkReason;

  @Getter @Setter
  private boolean online;

  private UserComponent[] components;

  private Player player;

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
  public Player getPlayer() {
    if (!isOnline()) {
      return null;
    }

    if (player == null) {
      player = Bukkit.getPlayer(getUniqueId());
    }

    return player;
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
  public Inventory getInventory() throws UserOfflineException {
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

  @Override
  public void playSound(Sound uiButtonClick, float volume, float pitch) {
    playSound(
        net.kyori.adventure.sound.Sound.sound()
            .type(uiButtonClick)
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
    if (timeFields == null) {
      return -1L;
    }

    if (timeFields.length <= field.getId()) {
      return -1L;
    }

    return timeFields[field.getId()];
  }

  @Override
  public void setTime(TimeField field, long value) {
    ensureValid();

    int id = field.getId();

    if (timeFields == null) {
      timeFields = new long[id + 1];
      Arrays.fill(timeFields, -1);
    } else if (timeFields.length <= id) {
      int oldLength = timeFields.length;
      int newLength = id + 1;

      timeFields = LongArrays.ensureCapacity(timeFields, id + 1);

      Arrays.fill(timeFields, oldLength, newLength, -1);
    }

    timeFields[id] = value;
  }

  @Override
  public Component displayName(@Nullable Audience viewer, boolean useNickname) {
    UserNameFactory factory = service.getNameFactory();
    return factory.formatDisplayName(this, viewer, useNickname ? ALLOW_NICKNAME : 0);
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
  public void setNickname(String nickname) {
    ensureValid();

    UserLookupImpl lookup = service.getLookup();
    UserLookupEntry entry = lookup.getEntry(uniqueId);

    lookup.onNickChange(entry, nickname);
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
  public void setAfk(boolean afk, @Nullable Component reason) throws UserOfflineException {
    ensureOnline();
    ensureValid();

    this.afk = afk;

    if (afk) {
      this.afkReason = reason;
    } else {
      this.afkReason = null;
    }
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
  public void afk(@Nullable Component reason) throws IllegalStateException, UserOfflineException {
    ensureOnline();
    Preconditions.checkState(!isAfk(), "User is already AFK");
    ensureValid();

    UserAfkEvent event = new UserAfkEvent(this, reason);
    event.callEvent();

    Component finalReason = event.getMessage();
    setAfk(true, finalReason);

    Users.forEachUser(user -> {
      if (user.equals(this)) {
        return;
      }

      Component viewerReason = finalReason;
      if (!event.getMessageViewFilter().test(user)) {
        viewerReason = null;
      }

      TextComponent.Builder builder = Component.text()
          .color(NamedTextColor.GRAY)
          .append(displayName(user));

      builder.append(Component.text(" is now AFK"));

      if (viewerReason != null) {
        builder.append(Component.text(": "))
            .append(viewerReason);
      }

      user.sendMessage(builder.build());
    });
  }

  @Override
  public void unafk() throws IllegalStateException, UserOfflineException {
    ensureOnline();
    Preconditions.checkState(afk, "User is not AFK");
    ensureValid();

    setAfk(false, null);
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

  @Override
  public boolean hasPermission(String permission) {
    if (isOnline()) {
      return getPlayer().hasPermission(permission);
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
          .checkPermission(permission)
          .asBoolean();
    }

    try {
      return lpManager.loadUser(getUniqueId())
          .get()
          .getCachedData()
          .getPermissionData(options)
          .checkPermission(permission)
          .asBoolean();
    } catch (ExecutionException | InterruptedException e) {
      LOGGER.error("Couldn't fetch permission data from LuckPerms", e);
    }

    return false;
  }

  @Override
  public boolean hasPermission(Permission permission) {
    return hasPermission(permission.getName());
  }

  @Override
  public void updateVanished() {
    ensureValid();
  }

  @Override
  public void updateGodMode() {
    ensureValid();
  }

  @Override
  public void updateFlying() {
    ensureValid();
  }

  @Override
  public void updateTabName() {
    ensureValid();

  }

  @Override
  public boolean checkTeleporting() {
    return false;
  }

  @Override
  public UserTeleport createTeleport(Supplier<Location> destination, Type type)
      throws UserOfflineException
  {
    ensureValid();
    return null;
  }

  @Override
  public boolean isTeleporting() {
    return false;
  }

  @Override
  public boolean canTeleport() {
    return false;
  }
}