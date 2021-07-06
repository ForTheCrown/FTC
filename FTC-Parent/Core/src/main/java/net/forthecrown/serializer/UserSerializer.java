package net.forthecrown.serializer;

import net.forthecrown.user.FtcUser;

import java.io.File;
import java.util.UUID;

public interface UserSerializer {
    void serialize(FtcUser user);
    void deserialize(FtcUser user);

    void onUnload(FtcUser user);

    void delete(FtcUser id);

    File getFile(UUID id);
}
