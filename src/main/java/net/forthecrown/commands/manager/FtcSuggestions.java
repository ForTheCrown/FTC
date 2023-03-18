package net.forthecrown.commands.manager;


import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;

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
    String token = builder.getRemainingLowerCase();

    // Only use cache suggestions if we allow offline users,
    // have more than 3 chars inputted and the userCacheSuggestions == true
    if (acceptOffline
        && token.length() >= 3
        && GeneralConfig.userCacheSuggestions
    ) {
      return UserManager.get().getUserLookup()
          .suggestNames(builder);
    }

    boolean seeVanished = c == null || c.hasPermission(Permissions.VANISH_SEE);

    for (User user : Users.getOnline()) {
      if (!seeVanished && user.get(Properties.VANISHED)) {
        continue;
      }

      // During parsing, names and nicknames are valid, so
      // suggest the one that matches what they're typing.
      // Check if we should suggest nicks before, as their
      // nick may just be a shortening of their name
      if (user.hasNickname()
          && Completions.matches(token, user.getNickname())
      ) {
        builder.suggest(user.getNickname(), uuidTooltip(user.getUniqueId()));
      } else if (Completions.matches(token, user.getName())) {
        builder.suggest(user.getName(), uuidTooltip(user.getUniqueId()));
      }
    }

    return builder.buildFuture();
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