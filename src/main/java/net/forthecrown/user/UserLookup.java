package net.forthecrown.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.core.FTC;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.bukkit.OfflinePlayer;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * A map of user profiles organized in ways
 * to allow for easy and fast lookup.
 * I don't really know how to describe this class.
 * <p>
 * This class organizes {@link UserLookupEntry} objects into
 * 4 maps. These 4 maps are made to allow for faster lookups
 * of user profiles by their identifiers, like their names,
 * nicknames and the last name they had.
 * <p>
 * This map's data is kept in sync with the following methods:
 * {@link #onNameChange(UserLookupEntry, String)} and {@link #onNickChange(UserLookupEntry, String)}
 */
public class UserLookup extends SerializableObject.AbstractSerializer<JsonArray> {
    private static final Logger LOGGER = FTC.getLogger();

    /**
     * Expected size of the 2 primary maps for tracking
     * names and UUIDs
     */
    public static final int EXPECTED_SIZE = 1100;

    /**
     * Result returned by {@link UserLookupEntry#getLastNameChange()} if the
     * user has not changed their name in the recent past
     */
    public static final long NO_NAME_CHANGE = -1;

    // Primary lookup maps
    private final Map<UUID,   UserLookupEntry> identified = new Object2ObjectOpenHashMap<>(EXPECTED_SIZE);
    private final Map<String, UserLookupEntry> named      = new Object2ObjectOpenHashMap<>(EXPECTED_SIZE);

    // Secondary lookup maps
    private final Map<String, UserLookupEntry> oldNamed   = new Object2ObjectOpenHashMap<>(30);
    private final Map<String, UserLookupEntry> nicknamed  = new Object2ObjectOpenHashMap<>(20);

    /**
     * If the changes have been made to this map and the
     * map has not been saved.
     */
    @Getter
    private boolean unsaved;

    public UserLookup(Path path) {
        super(path);
    }

    /**
     * Saves the cache to disk
     */
    public void save() {
        if (!isUnsaved()) {
            return;
        }

        SerializationHelper.writeFile(filePath, file -> {
            var array = new JsonArray();
            save(array);

            JsonUtils.writeFile(array, file);
        });
    }

    /**
     * Reloads the cache from disk
     */
    public void reload() {
        SerializationHelper.readFile(
                filePath,
                file -> JsonUtils.readFile(file).getAsJsonArray(),
                this::load
        );

        unsaved = false;
    }

    protected void save(JsonArray array) {
        entryStream()
                .forEach(reader -> array.add(reader.serialize()));

        unsaved = false;
    }

    protected void load(JsonArray array) {
        if (FTC.inDebugMode()) {
            LOGGER.info("Beginning profile map load");
        }

        StopWatch watch = StopWatch.createStarted();
        clear();

        for (JsonElement e: array) {
            UserLookupEntry entry = UserLookupEntry.deserialize(e);

            if (!Users.hasVanillaData(entry.getUniqueId())) {
                LOGGER.warn("Found player that has not played before, ID: {}, name: {}, deleting",
                        entry.getUniqueId(), entry.getName()
                );

                UserManager.get().getSerializer().delete(entry.getUniqueId());
                continue;
            }

            addEntry(entry);
        }

        watch.stop();
        if (FTC.inDebugMode()) {
            LOGGER.info("Loaded profile map, took {}ms", watch.getTime());
        }

        unsaved = false;
    }

    /**
     * Gets a cache entry by the user's name
     * @param name The name to lookup
     * @return The cache entry of for the name, null, if not cached
     */
    public synchronized UserLookupEntry getNamed(String name) {
        return named.get(name.toLowerCase());
    }

    /**
     * Gets a cache entry by its nickname
     * @param nick The nickname to lookup
     * @return The entry for the nick, null, if no nick with that name exists
     */
    public synchronized UserLookupEntry getNicked(String nick) {
        return nicknamed.get(nick.toLowerCase());
    }

    /**
     * Gets an entry by the name used before they changed it
     * @param oldName The user's old name
     * @return The entry, null if no name exists
     */
    public synchronized UserLookupEntry getByLastName(String oldName) {
        return oldNamed.get(oldName.toLowerCase());
    }

    /**
     * Gets an entry by the given UUID
     * @param uuid The UUID to get the entry of
     * @return The UUID entry, null, if no player with the given UUID has joined the server
     */
    public synchronized UserLookupEntry getEntry(UUID uuid) {
        return identified.get(uuid);
    }

    /**
     * Creates an entry, which is added to the cache,
     * with the given name and UUID
     *
     * @param uuid The UUID to create the entry with
     * @param name The name of the entry
     * @return The created and registered entry
    */
    public synchronized UserLookupEntry createEntry(UUID uuid, String name) {
        UserLookupEntry entry = new UserLookupEntry(uuid);
        entry.name = name;
        addEntry(entry);

        return entry;
    }

    /**
     * Adds the given entry to this cache
     * @param entry The entry to add.
     */
    private synchronized void addEntry(UserLookupEntry entry) {
        identified.put(entry.getUniqueId(), entry);
        named.put(entry.getName().toLowerCase(), entry);

        // If entry has nickname, add to nick lookup map
        if(entry.getNickname() != null) {
            nicknamed.put(entry.getNickname().toLowerCase(), entry);
        }

        // If user has a lastname, add to lastname lookup map
        if(entry.getLastName() != null) {
            oldNamed.put(entry.getLastName().toLowerCase(), entry);
        }

        unsaved = true;
    }

    /**
     * Called when a user joins the server with a
     * different name than the last name they had while
     * online
     * @param entry The entry that changed their name
     * @param newName the new name
     */
    public synchronized void onNameChange(UserLookupEntry entry, String newName) {
        if (entry.lastName != null) {
            oldNamed.remove(entry.lastName);
        }

        named.remove(entry.name.toLowerCase());

        entry.lastNameChange = System.currentTimeMillis();
        entry.lastName = entry.name;
        entry.name = newName;

        named.put(newName.toLowerCase(), entry);
        oldNamed.put(entry.lastName.toLowerCase(), entry);

        unsaved = true;
    }

    /**
     * Called when a user's nickname is changed
     * @param entry The entry that had its nick changed
     * @param newNick The new nickname
     */
    public synchronized void onNickChange(UserLookupEntry entry, String newNick) {
        if (entry.nickname != null) {
            nicknamed.remove(entry.nickname.toLowerCase());
        }

        entry.nickname = newNick;

        if (newNick != null) {
            nicknamed.put(newNick.toLowerCase(), entry);
        }

        unsaved = true;
    }

    /**
     * Clears the cache
     */
    public void clear() {
        named.clear();
        nicknamed.clear();
        identified.clear();
        oldNamed.clear();

        unsaved = true;
    }

    /**
     * Gets the total size of the cache
     * @return The cache's total size
     */
    public int size() {
        return identified.size();
    }

    /**
     * Gets a stream of all user profiles
     * @return A stream of all profiles
     */
    public Stream<UserLookupEntry> entryStream() {
        return identified.values().stream();
    }

    /**
     * Clears all 'invalid' entries.
     * <p>
     * An invalid entry is any entry that doesn't have a corresponding
     * {@link OfflinePlayer} object to it that has played on the server
     * before.
     */
    void clearInvalid() {
        LOGGER.info("clearInvalid called");
        var iterator = identified.entrySet().iterator();

        while (iterator.hasNext()) {
            var e = iterator.next();
            UserLookupEntry cache = e.getValue();

            if (!Users.hasVanillaData(e.getKey())) {
                LOGGER.info("{} has not played before, removing entry", e.getKey());

                iterator.remove();

                if (cache.getName() != null) {
                    named.remove(cache.getName());
                }

                if (cache.getNickname() != null) {
                    nicknamed.remove(cache.getNickname());
                }

                if (cache.getLastName() != null) {
                    oldNamed.remove(cache.getLastName());
                }

                UserManager.get()
                        .getSerializer()
                        .delete(e.getKey());
            }
        }
    }

    /**
     * Removes the given entry from the cache
     * @param cache The entry to remove
     */
    public void remove(UserLookupEntry cache) {
        identified.remove(cache.getUniqueId());
        named.remove(cache.getName());

        if (cache.getNickname() != null) {
            nicknamed.remove(cache.getNickname());
        }

        if (cache.getLastName() != null) {
            oldNamed.remove(cache.getLastName());
        }

        unsaved = true;
    }

    /**
     * Suggests display names from this cache to the given suggestion builder.
     * <p>
     * This will loop through each cache entry and check if their nickname/name/lastname
     * match the given input, if it does, it'll suggest the entry's nickname/name/lastname.
     * <p>
     * The name it suggests will correspond to the user's input, if they've started
     * typing the nickname of the entry and not the actual name, it'll suggest
     * the nickname instead of the actual name, like wise with the lastname
     * instead of the entry's actual name
     *
     * @param builder The builder to suggest to
     * @return The built suggestions
     */
    public CompletableFuture<Suggestions> suggestNames(SuggestionsBuilder builder) {
        var token = builder.getRemainingLowerCase();

        for (var e: identified.values()) {
            var hover = FtcSuggestions.uuidTooltip(e.getUniqueId());

            if (tokenMatches(e.getNickname(), token)) {
                builder.suggest(e.getNickname(), hover);
                continue;
            }

            if (tokenMatches(e.getName(), token)) {
                builder.suggest(e.getName(), hover);
                continue;
            }

            if (tokenMatches(e.getLastName(), token)) {
                builder.suggest(e.getLastName(), hover);
            }
        }

        return builder.buildFuture();
    }

    /**
     * Tests that the given string matches the given input token
     * @param s The string to test
     * @param token The token to test against
     * @return True, if the string is not null and matches the token
     */
    private static boolean tokenMatches(String s, String token) {
        if (Util.isNullOrBlank(s)) {
            return false;
        }

        return CompletionProvider.startsWith(token, s);
    }

    /**
     * Gets a cache entry by a string.
     * <p>
     * This first tries to find an entry by its name,
     * if that returns null, it will lookup the entry
     * by its last used name,
     * if that returns null, it will lookup the entry
     * by its nickname,
     * if that returns null, the function returns null.
     * <p>
     * If any step of that process does not return null, the found
     * entry is returned
     * @param str The string to use
     * @return The cache entry for the given string
     */
    public UserLookupEntry get(String str) {
        UserLookupEntry entry;

        if ((entry = getNamed(str)) != null) {
            return entry;
        }

        if ((entry = getByLastName(str)) != null) {
            return entry;
        }

        return getNicked(str);
    }

    /**
     * Creates an entry for the given player
     * @param player The player to create for
     * @return The created entry
     */
    public UserLookupEntry createEntry(OfflinePlayer player) {
        return createEntry(player.getUniqueId(), player.getName());
    }

    /**
     * Removes an entry with the given UUID from the cache
     * @param uuid The UUID to remove
     */
    public void remove(UUID uuid) {
        remove(getEntry(uuid));
    }

}