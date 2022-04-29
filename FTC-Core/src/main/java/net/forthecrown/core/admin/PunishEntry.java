package net.forthecrown.core.admin;

import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * An entry that tracks a user's punishments
 */
public interface PunishEntry extends JsonSerializable, JsonDeserializable {
    /**
     * Gets the user's past punishments
     * @return Past punishments
     */
    Collection<Punishment> past();

    /**
     * Gets the punishments that currently
     * affect this user
     * @return Current punishments
     */
    Collection<Punishment> current();

    /**
     * Gets all notes attached to this
     * entry
     * @return The entry's staff notes
     */
    List<EntryNote> notes();

    /**
     * Gets the holder of this entry
     * @return The entry's holder
     */
    UUID entryHolder();

    /**
     * Gets the user of the holder of this entry
     * @return The user that holds this entry
     */
    default CrownUser entryUser() {
        return UserManager.getUser(entryHolder());
    }

    /**
     * Gets a current punishment by its type
     * @param type The punishment type
     * @return The current punishment, null, if the entry has not been punished
     *         with the given type
     */
    Punishment getCurrent(PunishType type);

    /**
     * Checks if the entry is punished with the given type.
     * <p></p>
     * Because we can't rely 100% on the entry's records alone,
     * this also does a check with the {@link org.bukkit.BanList}
     * to see if they are banned
     *
     * @param type The type to check
     * @return True, if they are, false otherwise
     */
    boolean isPunished(PunishType type);

    /**
     * Punishes the entry with the given punishment
     * @param punishment The punishment to enforce
     */
    void punish(Punishment punishment);

    /**
     * Revokes the given punishment
     * @param type The type to revoke
     */
    void revokePunishment(PunishType type);
}