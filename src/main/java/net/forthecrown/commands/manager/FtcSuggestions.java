package net.forthecrown.commands.manager;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.royalgrenadier.WrappedCommandSource;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.VanillaAccess;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.forthecrown.grenadier.CompletionProvider.startsWith;

/**
 * Utility class for suggestions
 */
public interface FtcSuggestions {
    Suggester<CommandSource> COMMAND_SUGGESTIONS = (c, b) -> {
        StringReader reader = new StringReader(b.getInput());
        reader.setCursor(b.getStart());

        var dispatcher = VanillaAccess.getServer().getCommands()
                .getDispatcher();

        var parse = dispatcher.parse(
                reader,
                ((WrappedCommandSource) c.getSource()).getHandle()
        );

        return dispatcher.getCompletionSuggestions(parse);
    };

    /**
     * Suggests player names.
     * <p>
     * If the given input is larger than 5 characters, and <code>acceptOffline == true</code>,
     * this will ask the user cache to provide user name suggestions. Otherwise this
     * will simply loop through all online users and add their nickname or actual
     * name, depending on the input, to the suggestions.
     *
     * @param c The source asking for the suggestions
     * @param builder The builder to suggest to
     * @param acceptOffline True, if offline names may be suggested, false otherwise
     * @return The built suggestions
     */
    static CompletableFuture<Suggestions> suggestPlayerNames(CommandSource c, SuggestionsBuilder builder, boolean acceptOffline) {
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

        for (User user: Users.getOnline()) {
            if (!seeVanished && user.get(Properties.VANISHED)) {
                continue;
            }

            // During parsing, names and nicknames are valid, so
            // suggest the one that matches what they're typing.
            // Check if we should suggest nicks before, as their
            // nick may just be a shortening of their name
            if (user.hasNickname()
                    && startsWith(token, user.getNickname())
            ) {
                builder.suggest(user.getNickname(), uuidTooltip(user.getUniqueId()));
            }
            else if (startsWith(token, user.getName())) {
                builder.suggest(user.getName(), uuidTooltip(user.getUniqueId()));
            }
        }

        return builder.buildFuture();
    }

    /**
     * Returns the message displayed when the player name
     * suggestion is hovered over
     * @return The suggestion hover message
     */
    static Message uuidTooltip(UUID uuid) {
        return new LiteralMessage(uuid.toString());
    }
}