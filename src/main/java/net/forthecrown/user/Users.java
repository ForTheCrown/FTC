package net.forthecrown.user;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.experimental.UtilityClass;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.forthecrown.utils.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

/**
 * Utility class for methods related to users
 */
public @UtilityClass class Users {
    /* ---------------------------- USER GETTERS ---------------------------- */

    /**
     * Gets a user for a player
     * @param base The player to get the user of
     * @return The user of the base
     * @throws IllegalArgumentException If the player has not played on this server before
     */
    public User get(OfflinePlayer base) throws IllegalArgumentException {
        return get(base.getUniqueId());
    }

    /**
     * Gets a loaded user
     * @param base The UUID of the user
     * @return The loaded user, null, if the User attached to the given UUID is not loaded
     */
    public User getLoadedUser(UUID base) {
        return UserManager.get().getLoaded().get(base);
    }

    /**
     * Gets a user for the corresponding UUID
     * @param base The UUID to get the player of, will create a new user if it doesn't already exist
     * @return A user :I
     * @throws IllegalArgumentException If the given UUID does not belong to a user
     *                                  that has played on this server
     */
    public User get(@NotNull UUID base) throws IllegalArgumentException {
        Validate.notNull(base, "UUID cannot be null");
        UserLookupEntry entry = UserManager.get()
                .getUserLookup()
                .getEntry(base);

        Validate.notNull(entry, "Given UUID did not belong to a player: %s", base);

        return get(entry);
    }

    /**
     * Gets a user by the given cache entry
     * @param profile The cache entry
     * @return The gotten/created user
     * @throws IllegalArgumentException If the given profile is null
     */
    public User get(UserLookupEntry profile) throws IllegalArgumentException {
        return UserManager.get().getUser(profile);
    }

    /**
     * Same as the above 3 methods except it takes in a player's name
     * @param name The name/nickname/valid oldname of the user
     * @return The user, will throw an exception
     */
    public User get(String name) {
        return get(
                UserManager.get()
                        .getUserLookup()
                        .get(name)
        );
    }

    /* ----------------------------- UTILITY ------------------------------ */

    /**
     * Tests if the given UUID belongs to a player
     * that has played on the server.
     * <p>
     * This will just ensure that the given UUID has
     * a matching {@link UserLookupEntry} in the {@link UserLookup}
     * @param uuid The UUID to test
     * @return True, if the UUID belongs to a player, false otherwise
     */
    public boolean isPlayerId(UUID uuid) {
        return UserManager.get().getUserLookup().getEntry(uuid) != null;
    }

    /**
     * Gets all currently online players as users
     * @return All online users
     */
    public Set<User> getOnline() {
        return new ObjectOpenHashSet<>(
                UserManager.get()
                        .getOnline()
                        .values()
        );
    }

    /**
     * Unloads all currently loaded users that are online.
     * <p>
     * All users are saved before being potentially unloaded
     */
    public void unloadOffline() {
        var it = UserManager.get()
                .getLoaded()
                .entrySet()
                .iterator();

        while (it.hasNext()) {
            User u = it.next().getValue();
            u.save();

            if (!u.isOnline()) {
                it.remove();
            }
        }
    }

    /* ----------------------- BLOCKED/IGNORE TESTING ----------------------- */

    /**
     * Tests if the given user was blocked, is blocking, or was separated from
     * the target user. If they were blocked or separated, this method will send
     * the <code>sender</code> a message informing them that they were
     * <p>
     * Argument 0 on the 2 message format parameters will be the given target's
     * {@link User} object... That was a long way to say argument 0 is the
     * target.
     *
     * @see #testBlockedMessage(User, User, String, String)
     * @param sender The user
     * @param target The target user
     * @param senderIgnoredFormat The format to use if the sender has blocked
     *                            the target
     * @param targetIgnoredFormat The format to use if the target has blocked
     *                            the sender
     *
     * @return True, if either of the 2 users has blocked the other or have been
     *         separated, false otherwise
     */
    public boolean testBlocked(User sender,
                               User target,
                               String senderIgnoredFormat,
                               String targetIgnoredFormat
    ) {
        var optional = testBlockedMessage(
                sender, target,
                senderIgnoredFormat, targetIgnoredFormat
        );

        optional.ifPresent(format -> {
            sender.sendMessage(Text.format(format, NamedTextColor.GRAY, target));
        });

        return optional.isPresent();
    }

    /**
     * Tests if the given user was blocked, is blocking, or was separated from
     * the target user. If they were blocked or separated, this method will
     * throw a command syntax exception with the given formats.
     * <p>
     * Argument 0 on the 2 message format parameters will be the given target's
     * {@link User} object... That was a long way to say argument 0 is the
     * target.
     *
     * @see #testBlockedMessage(User, User, String, String)
     * @param sender The user
     * @param target The target user
     * @param senderIgnoredFormat The format to use if the sender has blocked
     *                            the target
     * @param targetIgnoredFormat The format to use if the target has blocked
     *                            the sender
     *
     * @throws CommandSyntaxException If the two users were separated or if
     *                                either had blocked the other
     */
    public void testBlockedException(User sender,
                                     User target,
                                     String senderIgnoredFormat,
                                     String targetIgnoredFormat
    ) throws CommandSyntaxException {
        var optional = testBlockedMessage(
                sender, target,
                senderIgnoredFormat, targetIgnoredFormat
        );

        if (optional.isEmpty()) {
            return;
        }

        throw Exceptions.format(optional.get(), target);
    }

    /**
     * Tests if the given user was blocked, is blocking, or was separated from
     * the target user. If they were blocked or separated, this method will
     * return the corresponding message from the 2 ignore formats given. If the
     * user and sender are forcefully separated, the result is
     * {@link Messages#SEPARATED_FORMAT}. If the users aren't blocked or
     * separated at all, an empty optional is returned
     * <p>
     * Argument 0 on the 2 message format parameters will be the given target's
     * {@link User} object... That was a long way to say argument 0 is the
     * target.
     *
     * @param sender The user
     * @param target The target user
     * @param senderIgnoredFormat The format to use if the sender has blocked
     *                            the target
     * @param targetIgnoredFormat The format to use if the target has blocked
     *                            the sender
     *
     * @return Corresponding ignore message, empty, if not blocked or separated
     *         in any way
     */
    public Optional<String> testBlockedMessage(User sender,
                                               User target,
                                               String senderIgnoredFormat,
                                               String targetIgnoredFormat
    ) {
        if (sender.equals(target)) {
            return Optional.empty();
        }

        var userInter = sender.getInteractions();
        var targetInter = target.getInteractions();

        if (userInter.isSeparatedPlayer(target.getUniqueId())) {
            return Optional.of(Messages.SEPARATED_FORMAT);
        }
        else if (userInter.isOnlyBlocked(target.getUniqueId())) {
            return Optional.of(senderIgnoredFormat);
        }
        else if (targetInter.isOnlyBlocked(sender.getUniqueId())) {
            return Optional.of(targetIgnoredFormat);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Tests if either of the 2 uses have blocked each other or
     * have been separated
     * @param sender The first user
     * @param target The second user
     * @return True, if either has blocked the other or they've
     *         been separated, false otherwise
     */
    public boolean areBlocked(User sender, User target) {
        if (sender.equals(target)) {
            return false;
        }

        var userInter = sender.getInteractions();
        var targetInter = target.getInteractions();

        return userInter.isBlockedPlayer(target.getUniqueId())
                || targetInter.isOnlyBlocked(sender.getUniqueId());
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Marries both of the given users
     * @param user The first user
     * @param target The second user
     */
    public void marry(User user, User target) {
        UserInteractions inter = user.getInteractions();
        UserInteractions tInter = target.getInteractions();

        inter.setSpouse(target.getUniqueId());
        inter.setWaitingFinish(null);

        tInter.setSpouse(user.getUniqueId());
        tInter.setWaitingFinish(null);

        target.sendMessage(Messages.nowMarried(user));
        user.sendMessage(Messages.nowMarried(target));

        Bukkit.getServer().sendMessage(
                Text.format("&e{0, user}&r is now married to &e{1, user}&r{2}",
                        user, target,

                        Util.RANDOM.nextInt(0, 1000) != 1 ?
                                text("!")
                                : text("... I give it a week", NamedTextColor.GRAY)
                        )
                );
    }

    /**
     * Tests if the given player ID has a vanilla data file
     * @param uuid The ID of the player to test
     * @return True, if the player has a vanilla data
     *         file, false if it does not
     */
    public boolean hasVanillaData(UUID uuid) {
        Path path = Paths.get("world", "playerdata", uuid.toString() + ".dat");
        return Files.exists(path);
    }

    /**
     * Tests if the given audience object allows ranks
     * in their chat.
     * <p>
     * This method accepts the following types as input
     * for a valid result: {@link CommandSource},
     * {@link User} and {@link Player}
     * @param audience The audience to test
     * @return True, if they allow ranks in their chat,
     *         false otherwise
     */
    public boolean allowsRankedChat(Audience audience) {
        if (audience instanceof CommandSource source) {
            return allowsRankedChat(source.asBukkit());
        }

        if (audience instanceof User user) {
            return user.get(Properties.RANKED_NAME_TAGS);
        }

        if (audience instanceof Player player) {
            return get(player).get(Properties.RANKED_NAME_TAGS);
        }

        return true;
    }

    /**
     * Creates a 'list' display name for the given user.
     * <p>
     * A 'list' display name is meant to be displayed in
     * the TAB menu, this means it features the full
     * prefix, name and suffix along with the click event
     * and hover event.
     * <p>
     * That being said, the resulting
     * name can be used anywhere for any reason
     *
     * @param user The user whom the name shall represent
     * @param displayName The display name to use
     * @param prependRank True, whether to allow a rank
     *                    prefix, false otherwise
     * @return The created 'list' display name.
     */
    public Component createListName(User user,
                                    Component displayName,
                                    boolean prependRank
    ) {
        var builder = text();
        var prefix = user.getEffectivePrefix(prependRank);

        if (!prefix.equals(empty())) {
            builder.append(prefix);
        }

        builder.append(displayName);

        if (user.getProperties().contains(Properties.SUFFIX)) {
            builder.append(user.get(Properties.SUFFIX));
        }

        if (user.isAfk()) {
            builder.append(Messages.AFK_SUFFIX);
        }

        return builder
                .style(nonItalic(NamedTextColor.WHITE))
                .hoverEvent(user)
                .clickEvent(user.getClickEvent())
                .build();
    }
}