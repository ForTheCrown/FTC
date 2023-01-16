package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.user.data.UserRanks;
import net.forthecrown.utils.UUID2IntMap;
import net.forthecrown.utils.UUID2IntMap.KeyValidator;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Getter
public final class UserManager implements SerializableObject {

  private static final Logger LOGGER = FTC.getLogger();

  private static final UserManager INSTANCE = new UserManager();

  /**
   * Map of all loaded users
   */
  final Map<UUID, User> loaded = new Object2ObjectOpenHashMap<>();

  /**
   * Map of online user's UUIDs to user
   */
  final Map<UUID, User> online = new Object2ObjectOpenHashMap<>();

  /**
   * User profile map.
   *
   * @see UserLookup
   */
  private final UserLookup userLookup;

  /**
   * User serializer
   * <p>
   * This is still an interface incase we ever need to change serialization formats again or start
   * serializing to a database instead of a file system. If we do need to do that, then we can just
   * switch out the current implementation of this serializer
   */
  private final UserSerializer serializer;

  /**
   * The user alt account list
   */
  private final AltUsers alts;

  /**
   * User balances map
   */
  private final UUID2IntMap balances;

  /**
   * User playtime map, measured in seconds
   */
  private final UUID2IntMap playTime;

  /**
   * User vote map
   */
  private final UUID2IntMap votes;

  /**
   * User gem map
   */
  private final UUID2IntMap gems;

  /**
   * The directory of that user files are in, not to be confused with the user data file directory
   * where individual user files are kept.
   * <p>
   * This is the directory where the {@link UUID2IntMap}s are stored and where {@link AltUsers} is
   * stored.
   */
  private final Path directory;

  public UserManager() {
    this.directory = PathUtil.getPluginDirectory("user");

    // Create user manager helpers
    serializer = new UserJsonSerializer(directory.resolve("data"));
    alts = new AltUsers(directory.resolve("alts.json"), this);
    userLookup = new UserLookup(directory.resolve("profiles.json"));

    // Create user data maps
    balances  = createMap("balances.json", () -> GeneralConfig.startRhines);
    playTime  = createMap("playtime.json", null);
    votes     = createMap("votes.json", null);
    gems      = createMap("gems.json", null);

    readExtraRanks();
  }

  private UUID2IntMap createMap(String file, @Nullable IntSupplier supplier) {
    Path path = directory.resolve(file);

    if (supplier == null) {
      return new UUID2IntMap(path)
          .setValidator(KeyValidator.IS_USER);
    }

    return new UUID2IntMap(path, supplier)
        .setValidator(KeyValidator.IS_USER);
  }

  private void readExtraRanks() {
    Path rankJson = directory.resolve("ranks.toml");

    try {
      FtcJar.saveResources("ranks.toml", rankJson);
    } catch (IOException exc) {
      LOGGER.error("Couldn't save {} defaults!", rankJson, exc);
    }

    SerializationHelper.readTomlAsJson(rankJson, wrapper -> {
      for (var e: wrapper.entrySet()) {
        if (!Keys.isValidKey(e.getKey())) {
          LOGGER.warn("{} is an invalid registry key", e.getKey());
          continue;
        }

        if (!(e.getValue().isJsonObject())) {
          LOGGER.warn("Expected {} to be JSON object, was {}",
              e.getKey(), e.getValue()
          );
          continue;
        }

        // IDK if it matters, but even though there's a init call in
        // Bootstrap, this is at the moment, the first call to this class
        // during startup, meaning this call loads the class
        UserRanks.parse(e.getValue())
            .resultOrPartial(s -> {
              LOGGER.warn("Couldn't parse rank at {}: {}", e.getKey(), s);
            })

            .ifPresent(rank -> UserRanks.REGISTRY.register(e.getKey(), rank));
      }
    });
  }

  public static UserManager get() {
    return INSTANCE;
  }

  /**
   * Saves all the user data maps
   */
  public void saveMaps() {
    balances.save();
    playTime.save();
    gems.save();
    votes.save();
  }

  /**
   * Loads all the user data maps
   */
  public void loadMaps() {
    balances.reload();
    playTime.reload();
    gems.reload();
    votes.reload();
  }

  /**
   * Saves all users, user maps, the user cache and user alt account list
   */
  @Override
  @OnSave
  public void save() {
    saveMaps();
    userLookup.save();
    alts.save();

    saveUsers();
  }

  /**
   * Reloads all users, user maps, the user cache and alt account list
   */
  @Override
  @OnLoad
  public void reload() {
    userLookup.reload();
    alts.reload();
    loadMaps();

    reloadUsers();
  }

  /**
   * Saves all users, just calls {@link Users#unloadOffline()} lol
   */
  public void saveUsers() {
    Users.unloadOffline();
  }

  /**
   * Reloads all currently loaded users
   */
  public void reloadUsers() {
    loaded.values().forEach(User::reload);
  }

  public User getUser(UserLookupEntry profile) {
    Objects.requireNonNull(profile, "Null player profile given!");
    UUID base = profile.getUniqueId();

    return getLoaded().computeIfAbsent(base, uuid -> {
      var main = alts.getMain(uuid);
      return main != null ? new UserAlt(uuid, main) : new User(uuid);
    });
  }

  /**
   * Removes the user by the given ID from this manager, aka, unloading it without saving the user
   * first.
   *
   * @param uuid The ID to remove, aka, unload
   */
  public void remove(UUID uuid) {
    loaded.remove(uuid);
    online.remove(uuid);
  }

  /**
   * Gets ALL offline and online users that have an entry in the {@link UserLookup}.
   * <p>
   * This WILL load all user files and as such is run asynchronously to prevent overwhelming the
   * main thread and possibly causing a server crash.
   * <p>
   * During the execution of this method, this method also tests that all {@link UserLookup} entries
   * are 'valid', meaning that the entry represents a player that has logged onto the server before.
   * If any entry is found that is 'invalid', then this method will call
   * {@link UserLookup#clearInvalid()} before it returns the loaded list.
   * <p>
   * It is <b>highly</b> advisable to call {@link Users#unloadOffline()} after any iteration or
   * modification of this method's resulting users is finished to not take up too much memory.
   *
   * @return A completable future completed once all users have been loaded
   */
  public CompletableFuture<List<User>> getAllUsers() {
    return CompletableFuture.supplyAsync(() -> {
      AtomicBoolean shouldRunValidationLoop = new AtomicBoolean(false);

      List<User> users = getUserLookup()
          .entryStream()
          .filter(entry -> {
            if (!Users.hasVanillaData(entry.getUniqueId())) {
              LOGGER.info("Found invalid player: {}, while running getAllUsers",
                  entry.getUniqueId());

              shouldRunValidationLoop.set(true);
              return false;
            }

            return true;
          })
          .map(this::getUser)
          .collect(ObjectArrayList.toList());

      if (shouldRunValidationLoop.get()) {
        userLookup.clearInvalid();
      }

      return users;
    });
  }

  /**
   * Saves and then unloads the given user
   *
   * @param user The user to unload
   */
  public void unload(User user) {
    user.save();
    remove(user.getUniqueId());

    user.clearComponents();
  }
}