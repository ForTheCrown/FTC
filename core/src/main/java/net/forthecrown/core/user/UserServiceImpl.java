package net.forthecrown.core.user;

import com.google.common.reflect.Reflection;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.core.CoreConfig;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.core.user.PropertyImpl.BuilderImpl;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.registry.RegistryListener;
import net.forthecrown.user.Properties;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserProperty;
import net.forthecrown.user.UserProperty.Builder;
import net.forthecrown.user.UserService;
import net.forthecrown.user.event.UserLeaveEvent;
import net.forthecrown.user.event.UserLogEvent;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.ScoreIntMap;
import net.forthecrown.utils.ScoreIntMap.KeyValidator;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.utils.io.PathUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Internal
@Getter
public class UserServiceImpl implements UserService {

  public static final Logger LOGGER = Loggers.getLogger();

  private final CorePlugin plugin;

  private final UserDataStorage storage;

  private final UserLookupImpl lookup;
  private final NameFactoryImpl nameFactory;
  private final UserMaps userMaps;
  private final AltUsers altUsers;

  private final ScoreIntMap<UUID> balances;
  private final ScoreIntMap<UUID> gems;
  private final ScoreIntMap<UUID> playtime;
  private final ScoreIntMap<UUID> votes;

  private final Registry<UserProperty<?>> propertyRegistry;

  public UserServiceImpl(CorePlugin plugin) {
    this.plugin = plugin;

    Path dir = PathUtil.pluginPath();
    this.storage     = new UserDataStorage(dir);

    this.lookup      = new UserLookupImpl();
    this.nameFactory = new NameFactoryImpl();
    this.userMaps    = new UserMaps(this);
    this.altUsers    = new AltUsers();

    this.balances = new ScoreIntMap<>();
    this.gems     = new ScoreIntMap<>();
    this.playtime = new ScoreIntMap<>();
    this.votes    = new ScoreIntMap<>();

    this.balances.setValidator(KeyValidator.IS_PLAYER);
    this.gems.setValidator(KeyValidator.IS_PLAYER);
    this.playtime.setValidator(KeyValidator.IS_PLAYER);
    this.votes.setValidator(KeyValidator.IS_PLAYER);

    this.propertyRegistry = Registries.newFreezable();
    this.propertyRegistry.setListener(new RegistryListener<>() {
      @Override
      public void onRegister(Holder<UserProperty<?>> value) {
        PropertyImpl<?> property = (PropertyImpl<?>) value.getValue();
        property.id = value.getId();
        property.key = value.getKey();
      }

      @Override
      public void onUnregister(Holder<UserProperty<?>> value) {
        PropertyImpl<?> property = (PropertyImpl<?>) value.getValue();
        property.id = -1;
        property.key = null;
      }
    });

  }

  public CoreConfig getConfig() {
    return plugin.getFtcConfig();
  }

  public void initialize() {
    // *Spongebob stinky sound effect*
    Reflection.initialize(Properties.class);

    registerComponent(PropertyMap.class);
    registerComponent(BlockListImpl.class);
  }

  public void shutdown() {
    save();

    userMaps.getOnline().forEach(user -> {
      onUserLeave(user, QuitReason.DISCONNECTED, false);
    });
  }

  public void save() {
    storage.saveMap(balances, storage.getBalances());
    storage.saveMap(gems,     storage.getGems());
    storage.saveMap(playtime, storage.getPlaytime());
    storage.saveMap(votes,    storage.getVotes());

    storage.saveProfiles(lookup);
    storage.saveAlts(altUsers);

    var userIt = userMaps.iterator();
    while (userIt.hasNext()) {
      var user = userIt.next();
      storage.saveUser(user);

      if (!user.isOnline()) {
        userIt.remove();
      }
    }
  }

  public void load() {
    storage.loadProfiles(lookup);
    storage.loadAlts(altUsers);

    storage.loadMap(balances, storage.getBalances());
    storage.loadMap(gems,     storage.getGems());
    storage.loadMap(playtime, storage.getPlaytime());
    storage.loadMap(votes,    storage.getVotes());

    for (UserImpl user : userMaps) {
      storage.loadUser(user);
    }
  }

  public void unloadUser(UserImpl user) {
    userMaps.remove(user);
  }

  public void onUserLeave(UserImpl user, QuitReason reason, boolean announce) {
    Player player = Bukkit.getPlayer(user.getUniqueId());
    Objects.requireNonNull(player, "Player not online: " + user.getName());

    UserLeaveEvent userEvent = new UserLeaveEvent(user, reason);
    userEvent.callEvent();

    if (announce) {
      UserLogEvent.maybeAnnounce(userEvent);
    }

    user.setEntityLocation(player.getLocation());
    user.setLastOnlineName(player.getName());

    if (user.vanishTicker != null) {
      user.vanishTicker.stop();
      user.vanishTicker = null;
    }

    // Log play time
    logTime(user).apply(LOGGER::error, seconds -> {
      long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();

      // Prevent excessive playtime bug
      if (TimeUnit.SECONDS.toMillis(seconds) > uptimeMillis) {
        LOGGER.warn(
            "Playtime bug in user {}, online for {} seconds ({} hours)???",
            user, seconds, TimeUnit.SECONDS.toHours(seconds)
        );

        return;
      } else {
        LOGGER.info(
            "Adding {} seconds or {} hours to {}'s playtime",
            seconds, TimeUnit.SECONDS.toHours(seconds), user
        );
      }

      playtime.add(user.getUniqueId(), seconds);
    });

    unloadUser(user);
  }

  /**
   * Logs the player's playtime
   */
  private Result<Integer> logTime(UserImpl user) {
    long join = user.getTime(TimeField.LAST_LOGIN);

    if (join == -1) {
      return Result.error("Join time not set");
    }

    var played = Time.timeSince(join) - user.getAfkTime();
    var timeSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(played);

    return Result.success(timeSeconds);
  }

  public void freezeRegistries() {
    propertyRegistry.freeze();
  }

  @Override
  public CompletableFuture<Collection<User>> loadAllUsers() {
    return CompletableFuture.supplyAsync(() -> {
      List<User> users = new ObjectArrayList<>();
      executeOnAllUserInternal(users::add, false);
      return users;
    });
  }

  @Override
  public void executeOnAllUsers(Consumer<User> operation) {
    executeOnAllUserInternal(operation, true);
  }

  @Override
  public void executeOnAllUsersAsync(Consumer<User> operation) {
    CompletableFuture.runAsync(() -> executeOnAllUserInternal(operation, true));
  }

  private void executeOnAllUserInternal(Consumer<User> operation, boolean unload) {
    lookup.stream().forEach(entry -> {
      UserImpl user = userMaps.getUser(entry);
      operation.accept(user);

      if (unload) {
        unloadUser(user);
      }
    });
  }

  @Override
  public Registry<UserProperty<?>> getUserProperties() {
    return propertyRegistry;
  }

  void ensureNotFrozen() {
    if (!propertyRegistry.isFrozen()) {
      return;
    }

    throw new IllegalStateException(
        "UserProperty registry is frozen and cannot be modified"
    );
  }

  @Override
  public Builder<UUID> createUuidProperty() {
    ensureNotFrozen();
    return new BuilderImpl<>(FtcCodecs.STRING_UUID);
  }

  @Override
  public Builder<Boolean> createBooleanProperty() {
    ensureNotFrozen();
    return new BuilderImpl<>(Codec.BOOL);
  }

  @Override
  public Builder<Component> createTextProperty() {
    ensureNotFrozen();
    return new BuilderImpl<>(FtcCodecs.COMPONENT);
  }

  @Override
  public <E extends Enum<E>> Builder<E> createEnumProperty(Class<E> type) {
    ensureNotFrozen();
    return new BuilderImpl<>(FtcCodecs.enumCodec(type));
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Collection<User> getOnlineUsers() {
    return (Collection) userMaps.getOnline();
  }

  @Override
  public void registerComponent(Class<? extends UserComponent> componentType) {
    Components.createFactory(componentType);
  }

  @Override
  public @NotNull UserImpl getUser(@NotNull UserLookup.LookupEntry entry) {
    return userMaps.getUser(entry);
  }

  @Override
  public @Nullable UUID getMainAccount(@NotNull UUID altPlayerId) {
    Objects.requireNonNull(altPlayerId);
    return altUsers.getMain(altPlayerId);
  }

  @Override
  public boolean isAltAccount(@NotNull UUID playerId) {
    Objects.requireNonNull(playerId);
    return altUsers.isAlt(playerId);
  }

  @Override
  public Collection<UUID> getOtherAccounts(@NotNull UUID playerId) {
    Objects.requireNonNull(playerId);
    return altUsers.getOtherAccounts(playerId);
  }

  @Override
  public boolean isAltForAny(@NotNull UUID playerId, @NotNull Collection<Player> players) {
    Objects.requireNonNull(playerId);
    Objects.requireNonNull(players);
    return altUsers.isAltForAny(playerId, players);
  }

  @Override
  public void setAltAccount(@NotNull UUID altPlayerId, @NotNull UUID mainPlayerId) {
    Objects.requireNonNull(altPlayerId);
    Objects.requireNonNull(mainPlayerId);
    altUsers.addEntry(altPlayerId, mainPlayerId);
  }

  @Override
  public void removeAltAccount(@NotNull UUID altPlayerId) {
    Objects.requireNonNull(altPlayerId);
    altUsers.removeEntry(altPlayerId);
  }
}