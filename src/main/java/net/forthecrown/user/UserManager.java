package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.forthecrown.core.AutoSave;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Vars;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public final class UserManager implements SerializableObject {
    private static final Logger LOGGER = Crown.logger();

    static UserManager inst;

    /**
     * Map of all loaded users
     */
    final Map<UUID, User> loadedUsers = new Object2ObjectOpenHashMap<>();

    /**
     * User profile map.
     * @see UserLookup
     */
    private final UserLookup userLookup;

    /**
     * User serializer
     * <p>
     * This is still an interface incase
     * we ever need to change serialization formats
     * again or start serializing to a database
     * instead of a file system. If we do need to
     * do that, then we can just switch out the
     * current implementation of this serializer
     */
    private final UserSerializer serializer;

    /**
     * The user alt account list
     */
    private final AltUsers alts;

    /** User balances map */
    private final UserScoreMap balances;

    /** User playtime map, measured in seconds */
    private final UserScoreMap playTime;

    /** User vote map */
    private final UserScoreMap votes;

    /** User gem map */
    private final UserScoreMap gems;

    /**
     * The directory of that user files are in, not
     * to be confused with the user data file directory
     * where individual user files are kept.
     * <p>
     * This is the directory where the {@link UserScoreMap}s are
     * stored and where {@link AltUsers} is stored.
     */
    private final Path directory;

    public UserManager() {
        this.directory = PathUtil.getPluginDirectory("user");

        // Create user manager helpers
        serializer = new UserJsonSerializer(directory.resolve("data"));
        alts = new AltUsers(directory.resolve("alts.json"), this);
        userLookup = new UserLookup(directory.resolve("profiles.json"));

        // Create user data maps
        balances = new UserScoreMap(directory.resolve("balances.json"), () -> Vars.startRhines);
        playTime = new UserScoreMap(directory.resolve("playtime.json"));
        votes    = new UserScoreMap(directory.resolve("votes.json"));
        gems     = new UserScoreMap(directory.resolve("gems.json"));

        AutoSave.get()
                .addCallback(this::save);

        // Reload tells all the just created
        // objects to load their data
        reload();
    }

    public static UserManager get() {
        return inst;
    }

    // Called in BootStrap via reflection
    private static void init() {
        inst = new UserManager();
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
     * Saves all users, user maps, the user cache
     * and user alt account list
     */
    @Override
    public void save() {
        saveMaps();
        userLookup.save();
        alts.save();

        saveUsers();
    }

    /**
     * Reloads all users, user maps, the user cache
     * and alt account list
     */
    @Override
    public void reload() {
        loadMaps();
        userLookup.reload();
        alts.reload();

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
        loadedUsers.values().forEach(User::reload);
    }

    /**
     * Removes the user by the given ID from this
     * manager, aka, unloading it without saving
     * the user first.
     * @param uuid The ID to remove, aka, unload
     */
    public void remove(UUID uuid) {
        loadedUsers.remove(uuid);
    }

    /**
     * Gets ALL offline and online users that have an entry in the
     * {@link UserLookup}.
     * <p>
     * This WILL load all user files and as such is run asynchronously
     * to prevent overwhelming the main thread and possibly causing
     * a server crash.
     * <p>
     * During the execution of this method, this method also tests that
     * all {@link UserLookup} entries are 'valid', meaning that the entry
     * represents a player that has logged onto the server before. If
     * any entry is found that is 'invalid', then this method will call
     * {@link UserLookup#clearInvalid()} before it returns the loaded
     * list.
     * <p>
     * It is <b>highly</b> advisable to call {@link Users#unloadOffline()}
     * after any iteration or modification of this method's resulting users
     * is finished to not take up too much memory.
     *
     * @return A completable future completed once all users have been loaded
     */
    public CompletableFuture<List<User>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            List<User> users = new ObjectArrayList<>();
            AtomicBoolean shouldRunValidationLoop = new AtomicBoolean(false);

            this.getUserLookup()
                    .entryStream()
                    .forEach(entry -> {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getUniqueId());

                        if (!p.hasPlayedBefore()) {
                            LOGGER.info("Found invalid player: {}, '{}', while running getAllUsers", p.getUniqueId(), p.getName());

                            shouldRunValidationLoop.set(true);
                            return;
                        }

                        users.add(Users.get(entry));
                    });

            if (shouldRunValidationLoop.get()) {
                userLookup.clearInvalid();
            }

            return users;
        });
    }

    /**
     * Saves and then unloads the given user
     * @param user The user to unload
     */
    public void unload(User user) {
        user.save();
        remove(user.getUniqueId());

        user.clearComponents();
    }
}