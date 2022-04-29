package net.forthecrown.core.admin;

import net.forthecrown.serializer.CrownSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The manager of things relating to punishments
 */
public interface Punisher extends CrownSerializer {
    /**
     * Gets an entry for a given UUID
     * @param uuid the UUID to get the entry of
     * @return The UUID's entry, will create an entry if there isn't one already
     */
    @Nonnull PunishEntry getEntry(UUID uuid);

    /**
     * Gets an entry for a given UUID
     * @param uuid The UUID to get the entry of
     * @return The UUID's entry, or null, if the UUID doesn't
     *         already have an entry
     */
    @Nullable PunishEntry getNullable(UUID uuid);

    /**
     * Gets a prisoner's jail cell
     * @param prisoner The prisoner's UUID
     * @return The jail the prisoner is in
     */
    @Nullable JailCell getCell(UUID prisoner);

    /**
     * Sets the given prisoner to be jailed in the given cell
     * @param prisoner The prisoner
     * @param cell The cell they're in
     */
    void setJailed(UUID prisoner, JailCell cell);

    /**
     * Removes the given prisoner from jail
     * @param prisoner The prisoner to free
     */
    void removeJailed(UUID prisoner);
}