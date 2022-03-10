package net.forthecrown.user;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

public interface UserCache {
    long NO_NAME_CHANGE = -1;

    /**
     * Gets a cache entry by the user's name
     * @param name The name to lookup
     * @return The cache entry of for the name, null, if not cached
     */
    CacheEntry getNamed(String name);

    /**
     * Gets a cache entry by its nickname
     * @param nick The nickname to lookup
     * @return The entry for the nick, null, if no nick with that name exists
     */
    CacheEntry getNicked(String nick);

    /**
     * Gets an entry by the name used before they changed it
     * @param oldName The user's old name
     * @return The entry, null if no name exists
     */
    CacheEntry getByLastName(String oldName);
    CacheEntry getEntry(UUID uuid);

    /**
     * Gets a cache entry by a string.
     * <p></p>
     * This first tries to find an entry by its name,
     * if that returns null, it will lookup the entry
     * by its last used name,
     * if that returns null, it will lookup the entry
     * by its nickname,
     * if that returns null, the function returns null.
     *
     * If any step of that process does not return null, the found
     * entry is returned
     * @param str The string to use
     * @return The cache entry for the given string
     */
    default CacheEntry get(String str) {
        CacheEntry entry = getNamed(str);
        if(entry != null) return entry;

        entry = getByLastName(str);
        if(entry != null) return entry;

        return getNicked(str);
    }

    /**
     * Creates an entry with the given name and UUID
     * @param uuid The UUID to create the entry with
     * @param name The name of the entry
     * @return The created and registered entry
     */
    CacheEntry createEntry(UUID uuid, String name);

    /**
     * Creates an entry for the given profile
     * @param profile The profile to create for
     * @return The created entry
     */
    default CacheEntry createEntry(PlayerProfile profile) {
        return createEntry(profile.getId(), profile.getName());
    }

    /**
     * Creates an entry for the given player
     * @param player The player to create for
     * @return The created entry
     */
    default CacheEntry createEntry(OfflinePlayer player) {
        return createEntry(player.getUniqueId(), player.getName());
    }

    /**
     * NOT API, called when a user joins the server with
     * a different name than the last name they had while
     * online
     * @param entry The entry that changed their name
     * @param newName the new name
     */
    void onNameChange(CacheEntry entry, String newName);

    /**
     * NOT API, called when a user changes their nickname,
     * or has their nickname changed
     * @param entry The entry that had its nick changed
     * @param newNick The new nickname
     */
    void onNickChange(CacheEntry entry, String newNick);

    /**
     * Gets the total size of the cache
     * @return The cache's total size
     */
    int size();

    /**
     * A stream of cache entries meant to be used
     * for serializing the cache
     * @return A stream of all caches
     */
    Stream<CacheEntry> readerStream();

    /**
     * Removes the given entry from the cache
     * @param entry The entry to remove
     */
    void remove(CacheEntry entry);

    /**
     * Removes an entry with the given UUID from the cache
     * @param uuid The UUID to remove
     */
    default void remove(UUID uuid) {
        remove(getEntry(uuid));
    }

    /**
     * An entry in the user cache
     */
    interface CacheEntry {
        /**
         * Gets the entry's UUID
         * @return the entry's UUID
         */
        UUID getUniqueId();

        /**
         * Gets the name of the entry
         * @return the entry's name
         */
        String getName();

        /**
         * Gets the entry's nickname
         * @return The entry's nickname,
         *         null, if the entry doesn't have a nickname
         */
        @Nullable
        String getNickname();

        /**
         * Gets the last name used by this user
         * @return The user's last used name,
         *         null, if the user has not changed their name
         */
        @Nullable
        String getLastName();

        /**
         * gets the last UNIX timestamp of when the user
         * changed their name
         * @return The last name change timestamp,
         *         {@link UserCache#NO_NAME_CHANGE}, if name was neverchanged
         */
        long getLastNameChange();
    }

}
