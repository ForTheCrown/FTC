package net.forthecrown.core.user;

import com.google.common.base.Strings;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.command.FtcSuggestions;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.Users;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;

@Internal
public class UserLookupImpl implements UserLookup {

  private static final Logger LOGGER = Loggers.getLogger();

  /**
   * Expected size of the 2 primary maps for tracking names and UUIDs
   */
  public static final int EXPECTED_SIZE = 1100;

  public static final long NO_NAME_CHANGE = -1;

  // Primary lookup maps
  private final Map<UUID, UserLookupEntry> identified
      = new Object2ObjectOpenHashMap<>(EXPECTED_SIZE);

  private final Map<String, UserLookupEntry> named
      = new Object2ObjectOpenHashMap<>(EXPECTED_SIZE);

  private final Map<String, UserLookupEntry> byIp
      = new Object2ObjectOpenHashMap<>(EXPECTED_SIZE);

  // Secondary lookup maps
  private final Map<String, UserLookupEntry> oldNamed
      = new Object2ObjectOpenHashMap<>(30);

  private final Map<String, UserLookupEntry> nicknamed
      = new Object2ObjectOpenHashMap<>(20);

  /**
   * If the changes have been made to this map and the map has not been saved.
   */
  @Getter @Setter
  private boolean unsaved;

  @Override
  public UserLookupEntry getEntry(UUID playerId) {
    var foundEntry = identified.get(playerId);

    if (foundEntry != null) {
      return foundEntry;
    }

    OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
    String name = player.getName();

    if (Strings.isNullOrEmpty(name)) {
      return null;
    }

    UserLookupEntry entry = createEntry(playerId, name);

    LOGGER.warn("Data of player {} or '{}' may have been dropped, attempted recovery",
        playerId, name
    );

    return entry;
  }

  @Override
  public UserLookupEntry query(String string) {
    string = string.toLowerCase();

    try {
      UUID uuid = UUID.fromString(string);
      return getEntry(uuid);
    } catch (IllegalArgumentException exc) {
      // Ignored, just means input wasn't a UUID string, move onto name queries
    }

    UserLookupEntry entry = named.get(string);

    if (entry != null) {
      return entry;
    }

    entry = nicknamed.get(string);

    if (entry != null) {
      return entry;
    }

    entry = byIp.get(string);

    if (entry != null && entry.isIpTrustable()) {
      return entry;
    }

    return oldNamed.get(string);
  }

  /**
   * Creates an entry, which is added to the cache, with the given name and UUID
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
   *
   * @param entry The entry to add.
   */
  public synchronized void addEntry(UserLookupEntry entry) {
    var existing = identified.get(entry.getUniqueId());

    if (existing != null) {
      remove(existing);
    }

    identified.put(entry.getUniqueId(), entry);
    named.put(entry.getName().toLowerCase(), entry);

    // If entry has nickname, add to nick lookup map
    if (entry.getNickname() != null) {
      nicknamed.put(entry.getNickname().toLowerCase(), entry);
    }

    // If user has a lastname, add to lastname lookup map
    if (entry.getLastName() != null) {
      oldNamed.put(entry.getLastName().toLowerCase(), entry);
    }

    if (entry.getIp() != null) {
      byIp.put(entry.getIp(), entry);
    }

    unsaved = true;
  }

  /**
   * Called when a user joins the server with a different name than the last name they had while
   * online
   *
   * @param entry   The entry that changed their name
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
   *
   * @param entry   The entry that had its nick changed
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

  public synchronized void changeIp(UserLookupEntry entry, String ip) {
    unsaved = true;

    String existing = entry.getIp();
    if (existing != null) {
      UserLookupEntry existingEntry = byIp.get(existing);
      if (existingEntry != null && existingEntry.uniqueId.equals(entry.uniqueId)) {
        byIp.remove(existing);
      }
    }

    entry.setIp(ip);

    if (ip == null) {
      entry.setLastIpUpdate(null);
      return;
    }

    entry.setLastIpUpdate(Instant.now());
    byIp.put(ip, entry);
  }

  /**
   * Removes the given entry from the cache
   *
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

    if (cache.getIp() != null) {
      byIp.remove(cache.getIp());
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

  @Override
  public CompletableFuture<Suggestions> suggestPlayerNames(
      CommandSource source,
      SuggestionsBuilder builder,
      boolean allowOffline
  ) {
    var token = builder.getRemainingLowerCase();

    if (token.length() < 3 || !allowOffline) {
      return Completions.suggest(builder,
          source.getVisiblePlayers()
              .map(Users::get)
              .map(User::getNickOrName)
      );
    }

    for (var e : identified.values()) {
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
        continue;
      }

      if (tokenMatches(e.getIp(), token)
          && source.hasPermission(CorePermissions.IP_QUERY)
          && e.isIpTrustable()
      ) {
        builder.suggest(e.getIp(), hover);
        continue;
      }
    }

    return builder.buildFuture();
  }

  /**
   * Tests that the given string matches the given input token
   *
   * @param s     The string to test
   * @param token The token to test against
   * @return True, if the string is not null and matches the token
   */
  private static boolean tokenMatches(String s, String token) {
    if (Strings.isNullOrEmpty(s)) {
      return false;
    }

    return Completions.matches(token, s);
  }

  @Override
  public Stream<UserLookupEntry> stream() {
    return identified.values().stream();
  }

  @Getter @Setter(AccessLevel.PACKAGE)
  @RequiredArgsConstructor
  public static class UserLookupEntry implements LookupEntry {

    private final UUID uniqueId;

    private String name;
    private String nickname;
    private String lastName;

    private String ip;
    private Instant lastIpUpdate;

    private long lastNameChange = NO_NAME_CHANGE;

    boolean isIpTrustable() {
      if (Strings.isNullOrEmpty(ip) || lastIpUpdate == null) {
        return false;
      }

      Instant invalidationDate = lastIpUpdate.plus(Duration.ofDays(14));
      Instant now = Instant.now();

      return now.isBefore(invalidationDate);
    }
  }
}