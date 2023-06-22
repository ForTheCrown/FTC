package net.forthecrown.user;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.forthecrown.grenadier.CommandSource;

public interface UserLookup {

  LookupEntry getEntry(UUID playerId);

  LookupEntry query(String string);

  CompletableFuture<Suggestions> suggestPlayerNames(
      CommandSource source,
      SuggestionsBuilder builder,
      boolean allowOffline
  );

  Stream<? extends LookupEntry> stream();

  interface LookupEntry {

    UUID getUniqueId();

    String getName();

    String getNickname();

    String getLastName();
  }
}