package net.forthecrown.core.admin;

import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PunishEntry extends JsonSerializable, JsonDeserializable {
    Collection<Punishment> past();

    Collection<Punishment> current();

    List<EntryNote> notes();

    UUID entryHolder();

    default CrownUser entryUser() {
        return UserManager.getUser(entryHolder());
    }

    default void stopCurrentTasks() {
        for (Punishment p : current()) {
            p.cancelTask();
        }
    }

    Punishment getCurrent(PunishType type);

    boolean isPunished(PunishType type);

    void punish(Punishment punishment);

    void revokePunishment(PunishType type);
}