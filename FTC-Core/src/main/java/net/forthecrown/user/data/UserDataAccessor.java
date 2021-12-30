package net.forthecrown.user.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface UserDataAccessor {
    @NotNull Key accessKey();
}
