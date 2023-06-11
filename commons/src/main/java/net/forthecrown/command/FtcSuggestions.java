package net.forthecrown.command;


import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.user.Users;

/**
 * Utility class for suggestions
 */
public interface FtcSuggestions {

  SuggestionProvider<CommandSource> COMMAND_SUGGESTIONS 
      = Grenadier.suggestAllCommands();

  /**
   * Suggests player names.
   * <p>
   * If the given input is larger than 5 characters, and <code>acceptOffline == true</code>, this
   * will ask the user cache to provide user name suggestions. Otherwise this will simply loop
   * through all online users and add their nickname or actual name, depending on the input, to the
   * suggestions.
   *
   * @param c             The source asking for the suggestions
   * @param builder       The builder to suggest to
   * @param acceptOffline True, if offline names may be suggested, false otherwise
   * @return The built suggestions
   */
  static CompletableFuture<Suggestions> suggestPlayerNames(
      CommandSource c,
      SuggestionsBuilder builder,
      boolean acceptOffline
  ) {
    return Users.getService().getLookup().suggestPlayerNames(c, builder, acceptOffline);
  }

  /**
   * Returns the message displayed when the player name suggestion is hovered over
   *
   * @return The suggestion hover message
   */
  static Message uuidTooltip(UUID uuid) {
    return new LiteralMessage(uuid.toString());
  }

  static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder,
                                                Map<String, String> map
  ) {
    String token = builder.getRemainingLowerCase();

    map.entrySet()
        .stream()
        .filter(e -> Completions.matches(token, e.getKey()))
        .forEach(entry -> {
          builder.suggest(entry.getKey(), entry::getValue);
        });

    return builder.buildFuture();
  }
}