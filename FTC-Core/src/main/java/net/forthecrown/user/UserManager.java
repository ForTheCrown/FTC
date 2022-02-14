package net.forthecrown.user;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.UserSerializer;
import net.forthecrown.user.actions.UserActionHandler;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the class manages users and their ALT accounts
 */
public interface UserManager extends CrownSerializer {
    /**
     * Gets a user for a player
     * @param base The player to get the user of
     * @return The user :|
     */
    static CrownUser getUser(Player base){
        return getUser(base.getUniqueId());
    }

    /**
     * Gets a user for a player
     * @param base The player to get the user of
     * @return obvious innit
     */
    static CrownUser getUser(OfflinePlayer base){
        return getUser(base.getUniqueId());
    }

    /**
     * Gets a user for the corresponding UUID
     * @param base The UUID to get the player of, will create a new user if it doesn't already exist
     * @return A user :I
     */
    static CrownUser getUser(@NotNull UUID base) {
        Validate.notNull(base, "UUID cannot be null");
        Validate.isTrue(isPlayerID(base), "Given UUID did not belong to a player");

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
        OfflinePlayer player = Bukkit.getOfflinePlayer(id);
        return player != null && player.getName() != null;
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
        return ListUtils.convertToSet(Bukkit.getOnlinePlayers(), UserManager::getUser);
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

    UserActionHandler getActionHandler();
    UserSerializer getSerializer();

    /**
     * Unloads all offline users
     */
    void unloadOffline();
}