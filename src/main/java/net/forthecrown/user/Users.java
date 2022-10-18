package net.forthecrown.user;

import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class for methods related to users
 */
public final class Users {
    private Users() {}

    /* ----------------------------- USER GETTERS ------------------------------ */

    /**
     * Gets a user for a player
     * @param base The player to get the user of
     * @return The user of the base
     * @throws IllegalArgumentException If the player has not played on this server before
     */
    public static User get(OfflinePlayer base) throws IllegalArgumentException {
        return get(base.getUniqueId());
    }

    /**
     * Gets a loaded user
     * @param base The UUID of the user
     * @return The loaded user, null, if the User attached to the given UUID is not loaded
     */
    public static User getLoadedUser(UUID base) {
        return UserManager.get().loadedUsers.get(base);
    }

    /**
     * Gets a user for the corresponding UUID
     * @param base The UUID to get the player of, will create a new user if it doesn't already exist
     * @return A user :I
     * @throws IllegalArgumentException If the given UUID does not belong to a user
     *                                  that has played on this server
     */
    public static User get(@NotNull UUID base) throws IllegalArgumentException {
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
    public static User get(UserLookupEntry profile) throws IllegalArgumentException {
        Validate.notNull(profile, "Null player profile given!");

        UUID base = profile.getUniqueId();

        return UserManager.get().loadedUsers.computeIfAbsent(base, uuid -> {
            var alts = UserManager.get().getAlts();
            var main = alts.getMain(uuid);

            return main != null ? new UserAlt(uuid, main) : new User(uuid);
        });
    }

    /**
     * Same as the above 3 methods except it takes in a player's name
     * @param name The name/nickname/valid oldname of the user
     * @return The user, will throw an exception
     */
    public static User get(String name) {
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
    public static boolean isPlayerId(UUID uuid) {
        return UserManager.get().getUserLookup().getEntry(uuid) != null;
    }

    /**
     * Gets all currently online players as users
     * @return All online users
     */
    public static Set<User> getOnline() {
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(player -> getLoadedUser(player.getUniqueId()))
                .collect(Collectors.toSet());
    }

    /**
     * Unloads all currently loaded users that are online.
     * <p>
     * All users are saved before being potentially unloaded
     */
    public static void unloadOffline() {
        var it = UserManager.get().loadedUsers.entrySet().iterator();

        while (it.hasNext()) {
            User u = it.next().getValue();
            u.save();

            if (!u.isOnline()) {
                it.remove();
            }
        }
    }

    /**
     * Tests if the given user was blocked, is blocking, or was
     * separated from the target user.
     * <p>
     * Argument 0 on the 2 message format parameters will be
     * the given target's {@link User} object... That was a
     * long way to say argument 0 is the target.
     *
     * @param sender The user
     * @param target The target user
     * @param senderIgnoredFormat The format to use if the sender has blocked the target
     * @param targetIgnoredFormat The format to use if the target has blocked the sender
     * @return True, if either of the 2 users has blocked the other or
     *         have been separated, false otherwise
     */
    public static boolean testBlocked(User sender, User target,
                                      String senderIgnoredFormat,
                                      String targetIgnoredFormat
    ) {
        var userInter = sender.getInteractions();
        var targetInter = target.getInteractions();
        String format;

        if (userInter.isSeparatedPlayer(target.getUniqueId())) {
            format = Messages.SEPARATED_FORMAT;
        } else if (userInter.isOnlyBlocked(target.getUniqueId())) {
            format = senderIgnoredFormat;
        } else if (targetInter.isOnlyBlocked(sender.getUniqueId())) {
            format = targetIgnoredFormat;
        } else {
            return false;
        }

        sender.sendMessage(Text.format(format, NamedTextColor.GRAY, target));
        return true;
    }

    /**
     * Tests if either of the 2 uses have blocked each other or
     * have been separated
     * @param sender The first user
     * @param target The second user
     * @return True, if either has blocked the other or they've
     *         been separated, false otherwise
     */
    public static boolean areBlocked(User sender, User target) {
        var userInter = sender.getInteractions();
        var targetInter = target.getInteractions();

        return userInter.isBlockedPlayer(target.getUniqueId())
                || targetInter.isOnlyBlocked(sender.getUniqueId());
    }

    /**
     * Marries both of the given users
     * @param user The first user
     * @param target The second user
     */
    public static void marry(User user, User target) {
        UserInteractions inter = user.getInteractions();
        UserInteractions tInter = target.getInteractions();

        inter.setSpouse(target.getUniqueId());
        inter.setWaitingFinish(null);

        tInter.setSpouse(user.getUniqueId());
        tInter.setWaitingFinish(null);

        user.setTimeToNow(TimeField.MARRIAGE_CHANGE);
        target.setTimeToNow(TimeField.MARRIAGE_CHANGE);

        target.sendMessage(Messages.nowMarried(user));
        user.sendMessage(Messages.nowMarried(target));

        Bukkit.getServer()
                .sendMessage(
                        Text.format("&e{0, user}&r is now married to &e{1, user}&r{2}",
                                user, target,

                                Util.RANDOM.nextInt(0, 1000) != 1 ?
                                        Component.text("!") :
                                        Component.text("... I give it a week", NamedTextColor.GRAY)
                        )
                );
    }

    public static boolean hasVanillaData(UUID uuid) {
        Path path = Paths.get("world", "playerdata", uuid.toString() + ".dat");
        return Files.exists(path);
    }
}