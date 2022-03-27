package net.forthecrown.core.admin;

import net.forthecrown.serializer.CrownSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface Punisher extends CrownSerializer {
    @Nonnull PunishEntry getEntry(UUID uuid);

    @Nullable JailCell getCell(UUID prisoner);
    void setJailed(UUID prisoner, JailCell cell);
    void removeJailed(UUID prisoner);

    default boolean isJailed(UUID prisoner) {
        return getCell(prisoner) != null;
    }
}