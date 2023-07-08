package net.forthecrown.user;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.forthecrown.grenadier.CommandSource;

/**
 * User lookup service
 */
public interface UserLookup {

  /**
   * Gets a lookup entry by its UUID
   *
   * @param playerId Player's UUID
   * @return Lookup entry
   */
  @Nullable LookupEntry getEntry(UUID playerId);

  /**
   * Queries a lookup entry from the lookup.
   * <p>
   * If the specified {@code string} can be parsed into a {@link UUID}, then it will be, and the
   * result will be returned from that id.
   * <p>
   * Otherwise, an entry is queried from a lookup map. Because of that, the specified {@code string}
   * can be a username, nickname or a user's previous name
   *
   * @param string Lookup string
   *
   * @return Found entry, or {@code null}, if no entry was found
   */
  @Nullable LookupEntry query(String string);

  /**
   * Suggests usernames, nicknames or lastnames, whichever one matches the input of the specified
   * {@code builder} the most
   *
   * @param source Source getting suggestions
   * @param builder Suggestions builder
   * @param allowOffline {@code true}, if the names of offline users can also be suggested,
   *                     {@code false}, otherwise
   *
   * @return Built suggestions
   */
  CompletableFuture<Suggestions> suggestPlayerNames(
      CommandSource source,
      SuggestionsBuilder builder,
      boolean allowOffline
  );

  /**
   * Gets a stream of all lookup entries
   * @return Entry stream
   */
  Stream<? extends LookupEntry> stream();

  /**
   * User entry in the lookup
   */
  interface LookupEntry {

    /**
     * Gets the user's UUID
     * @return User ID
     */
    UUID getUniqueId();

    /**
     * Gets the user's name
     * @return User name
     */
    String getName();

    /**
     * Gets the user's nickname
     * @return User's nickname, or {@code null}, if the user has no nickname
     */
    @Nullable String getNickname();

    /**
     * Gets the user's previous name
     * @return User's previous name, or {@code null}, if the user has not changed their name in
     *         the past 2 weeks
     */
    @Nullable String getLastName();
  }
}