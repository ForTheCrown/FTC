package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonWrapper;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.*;


@RequiredArgsConstructor
public class PunishEntry implements JsonSerializable {
    /**
     * The ID of the player this entry belongs to
     */
    @Getter
    private final UUID holder;

    /**
     * All current punishments active for the
     * holder of this entry.
     * <p>
     * Stored as final array with the index here being the ordinal
     * of the {@link PunishType} of the punishment entry
     */
    private final Punishment[] current = new Punishment[PunishType.TYPES.length];

    /**
     * The expired/pardoned punishments this user was punished
     * with in the past
     */
    @Getter
    private final List<Punishment> past = new ObjectArrayList<>();

    /**
     * Staff notes attached to this entry
     */
    @Getter
    private final List<EntryNote> notes = new ObjectArrayList<>();

    /**
     * Gets an immutable list of the punishments
     * that currently affect this user.
     * <p>
     * Because the current punishments are stored
     * in an array for <i>slightly faster</i> operations,
     * this function will create a new array, filter out
     * all null entries in the {@link #current} array
     * and then return the created array wrapped in a list.
     * @return Current punishments
     */
    public List<Punishment> getCurrent() {
        if (!hasCurrent()) {
            return Collections.emptyList();
        }

        return Arrays.stream(current)
                .filter(Objects::isNull)
                .collect(ObjectImmutableList.toList());
    }

    /**
     * Gets a current punishment by its type
     *
     * @param type The punishment type
     * @return The current punishment, null, if the entry has not been punished
     * with the given type
     */
    public Punishment getCurrent(PunishType type) {
        return current[type.ordinal()];
    }

    /**
     * Checks if the entry is punished with the given type.
     * <p>
     * Because FTC's records cannot be relyed on for 100%
     * accuracy, if the given type is either {@link PunishType#BAN}
     * or {@link PunishType#IP_BAN} then this method will
     * check the corresponding bukkit {@link BanList} if
     * the user is contained in that.
     * <p>
     * If the given type is not a ban type, then this will
     * check the active punishment list for this entry
     * get the result
     *
     * @param type The type to check
     * @return True, if they are, false otherwise
     */
    public boolean isPunished(PunishType type) {
        // Bans have to be treated differently as
        // They are recorded in minecraft's own ban
        // list, to ensure we're returning a correct
        // result, we need to check these maps as well.
        if (type == PunishType.BAN) {
            return Bukkit.getBanList(BanList.Type.NAME).isBanned(getUser().getName());
        }

        if (type == PunishType.IP_BAN) {
            return Bukkit.getBanList(BanList.Type.IP).isBanned(getUser().getIp());
        }

        // If we're not checking bans, then just check if we
        // have or don't have a current punishment active
        // with the given type
        return getCurrent(type) != null;
    }

    /**
     * Punishes the entry with the given punishment
     *
     * @param punishment The punishment to enforce
     */
    public void punish(Punishment punishment) {
        var type = punishment.getType();
        revokePunishment(type, null);

        current[type.ordinal()] = punishment;
        punishment.startTask(() -> revokePunishment(type, null));
    }

    /**
     * Revokes the given punishment
     *
     * @param type The type to revoke
     * @param pardonSource The name of the source of the pardon,
     *                     if this param is null, the punishment
     *                     being revoked will not be considered
     *                     pardoned
     */
    public void revokePunishment(PunishType type, @Nullable String pardonSource) {
        if (!isPunished(type)) {
            return;
        }

        Punishment punishment = getCurrent(type);
        current[type.ordinal()] = null;

        if (punishment != null) {
            punishment.cancelTask();
            past.add(0, punishment);

            if (pardonSource != null) {
                punishment.onPardon(pardonSource);
            }
        }

        type.onPunishmentEnd(getUser(), Punishments.get());
    }

    void clearCurrent() {
        var it = ArrayIterator.modifiable(current);

        while (it.hasNext()) {
            var next = it.next();
            next.cancelTask();
            it.remove();
        }
    }

    public boolean hasCurrent() {
        return ArrayIterator.unmodifiable(current).hasNext();
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.create();

        if (!notes.isEmpty()) {
            json.addList("notes", notes);
        }

        if (hasCurrent()) {
            json.addList("current", getCurrent());
        }

        if (!past.isEmpty()) {
            json.addList("past", getPast());
        }

        return json.nullIfEmpty();
    }

    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        notes.clear();
        past.clear();
        clearCurrent();

        notes.addAll(json.getList("notes", EntryNote::read));
        past.addAll(json.getList("past", Punishment::read));

        Collection<Punishment> list = json.getList("current", Punishment::read);
        for (Punishment p : list) {
            punish(p);
        }
    }

    /**
     * Gets the user of the holder of this entry
     * @return The user that holds this entry
     */
    public User getUser() {
        return Users.get(getHolder());
    }
}