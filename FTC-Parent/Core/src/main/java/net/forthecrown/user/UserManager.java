package net.forthecrown.user;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents the class manages users and their ALT accounts
 */
public interface UserManager extends CrownSerializer {

    /**
     * Gets the current instance of the UserManager
     * @return the current UserManager instance, same as FtcCore.getUserManager(); lol
     */
    static @NotNull UserManager inst(){
        return ForTheCrown.getUserManager();
    }

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
     * <p>Be careful, as this doesn't check if the UUID belongs to a player or not lol</p>
     * @param base The UUID to get the player of, will create a new user if it doesn't already exist
     * @return A user :I
     */
    static CrownUser getUser(@NotNull UUID base) {
        Validate.notNull(base, "UUID cannot be null");
        if(FtcUserManager.LOADED_USERS.containsKey(base)) return FtcUserManager.LOADED_USERS.get(base);
        return inst().isAlt(base) ? new FtcUserAlt(base, inst().getMain(base)) : new FtcUser(base);
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
     * Gets all currently loaded users
     * @return The currently loaded users
     */
    static Collection<CrownUser> getLoadedUsers(){
        return new ArrayList<>(FtcUserManager.LOADED_USERS.values());
    }

    /**
     * Gets all currently online players as users
     * @return All online users
     */
    static Set<CrownUser> getOnlineUsers(){
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

        getVanishedUsers().forEach(u -> user.getPlayer().hidePlayer(ForTheCrown.inst(), u.getPlayer()));
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

    //Gets all the currently online NMS players
    private static List<ServerPlayer> getSpectators() {
        List<ServerPlayer> list = new ArrayList<>();

        for (Player p: Bukkit.getOnlinePlayers()){
            if(p.getGameMode() != GameMode.SPECTATOR) continue;
            list.add(((CraftPlayer) p).getHandle());
        }

        return list;
    }
}
