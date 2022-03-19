package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.UserSerializer;
import net.forthecrown.user.actions.UserActionHandler;
import net.forthecrown.utils.FtcUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the class manages users and their ALT accounts
 */
public interface UserManager extends CrownSerializer {
    /**
     * Gets a user for a player
     * @param base The player to get the user of
     * @return obvious innit
     */
    static CrownUser getUser(OfflinePlayer base){
        return getUser(base.getUniqueId());
    }

    /**
     * Gets a loaded user
     * @param base The UUID of the user
     * @return The loaded user, null, if the User attached to the given UUID is not loaded
     */
    static CrownUser getLoadedUser(UUID base) {
        return FtcUserManager.LOADED_USERS.get(base);
    }

    /**
     * Gets a user for the corresponding UUID
     * @param base The UUID to get the player of, will create a new user if it doesn't already exist
     * @return A user :I
     */
    static CrownUser getUser(@NotNull UUID base) {
        Validate.notNull(base, "UUID cannot be null");
        UserCache.CacheEntry entry = Crown.getUserManager().getCache().getEntry(base);

        Validate.notNull(entry, "Given UUID did not belong to a player");

        return getUser(entry);
    }

    /**
     * Gets a user by the given cache entry
     * @param reader The cache entry
     * @return The gotten/created user
     */
    static CrownUser getUser(UserCache.CacheEntry reader) {
        UUID base = reader.getUniqueId();

        return FtcUserManager.LOADED_USERS.computeIfAbsent(base, uuid -> {
            UserManager manager = Crown.getUserManager();
            return manager.isAlt(uuid) ? new FtcUserAlt(uuid, manager.getMain(uuid)) : new FtcUser(uuid);
        });
    }

    /**
     * Same as the above 3 methods except it takes in a player's name
     * @param name
     * @return
     */
    static CrownUser getUser(String name){
        return getUser(FtcUtils.uuidFromName(name));
    }

    /**
     * Checks if the given UUID belongs to a player
     * @param id The id to check
     * @return Whether the UUID is one of a player or not
     */
    static boolean isPlayerID(UUID id) {
        return Crown.getUserManager().getCache().getEntry(id) != null;
    }

    /**
     * Gets all currently loaded users
     * @return The currently loaded users
     */
    static Collection<CrownUser> getLoadedUsers() {
        return new ArrayList<>(FtcUserManager.LOADED_USERS.values());
    }

    /**
     * Gets all currently online players as users
     * @return All online users
     */
    static Set<CrownUser> getOnlineUsers() {
        Set<CrownUser> online = new ObjectOpenHashSet<>();

        for (FtcUser u: FtcUserManager.LOADED_USERS.values()) {
            if(!u.isOnline()) continue;
            online.add(u);
        }

        return online;
    }

    /**
     * Gets all vanished players
     * @return All vanished players
     */
    static Set<CrownUser> getVanishedUsers(){
        Set<CrownUser> set = new HashSet<>();

        for (CrownUser u: getOnlineUsers()){
            if(u.isVanished()) set.add(u);
        }

        return set;
    }

    /**
     * Updates vanished players from the perspective of the given user
     * @param user The user to update vanished players for
     */
    static void updateVanishedFromPerspective(CrownUser user){
        if(!user.isOnline()) return;
        if(user.hasPermission(Permissions.VANISH_SEE)) return;

        getVanishedUsers().forEach(u -> user.getPlayer().hidePlayer(Crown.inst(), u.getPlayer()));
    }

    /**
     * Saves every user object to file
     */
    void saveUsers();

    /**
     * Reloads all user objects from file
     */
    void reloadUsers();

    /**
     * Gets the main account for the provided ALT UUID
     * @param id AltAccount UUID
     * @return the alt's main, null if no main exists or if it, itself is a main
     */
    UUID getMain(UUID id);

    /**
     * Checks if a UUID belongs to an alt account
     * @param id
     * @return True if it is, false if it isn't lol
     */
    boolean isAlt(UUID id);

    /**
     * Checks if the given ID is an alt account for any of the given players
     * @param id The alt ID to check
     * @param players The players to check
     * @return if the given ID is an alt for any of the players
     */
    boolean isAltForAny(UUID id, Collection<Player> players);

    /**
     * Checks if the given ID is a main account for any of the given players
     * @param id The main ID to check
     * @param players The players to check
     * @return if the given ID is a main for any of the players
     */
    boolean isMainForAny(UUID id, Collection<Player> players);

    /**
     * Gets all of the provided UUID's alt accounts
     * @param main the main account's UUID
     * @return All of the known alts that belong to it
     */
    List<UUID> getAlts(UUID main);

    /**
     * Registers the first UUID as an alt for the second
     * @param alt
     * @param main
     */
    void addEntry(UUID alt, UUID main);

    /**
     * Unregisters the UUID as an alt
     * @param alt
     */
    void removeEntry(UUID alt);

    /**
     * Gets all uses that have ever logged onto the server.
     * This WILL load all user files into the manager's tracking.
     * <p></p>
     * This is run async because I this can't be good to run sync
     * lmao, there's so many users and this has to load each one of
     * their files.
     * <p></p>
     * It is heavily adviseable to unload all non-online users
     * after any modification or interaction with the user objects
     *
     * @return All users that have user files in the user data directory
     */
    CompletableFuture<List<CrownUser>> getAllUsers();

    /**
     * Gets the user action handler
     * @return The user action handler
     */
    UserActionHandler getActionHandler();

    /**
     * Gets the user serializer
     * @return User serializer
     */
    UserSerializer getSerializer();

    /**
     * Gets the user cache
     * @return The user cache
     */
    UserCache getCache();

    /**
     * Gets the user cache file
     * @return The user cache file
     */
    File getCacheFile();

    /**
     * Saves the user cache
     */
    void saveCache();

    /**
     * Loads the user cache
     */
    void loadCache();

    /**
     * Unloads all offline users
     */
    void unloadOffline();
}
