package net.forthecrown.valhalla;

import net.forthecrown.valhalla.data.VikingRaid;
import net.kyori.adventure.key.Key;

import java.io.File;
import java.io.IOException;

public interface RaidSerializer {
    VikingRaid deserialize(String keyValue) throws IOException;
    void serialize(VikingRaid raid);

    File getFile(String keyValue) throws IOException;

    default VikingRaid deserialize(Key key) throws IOException {
        return deserialize(key.value());
    }

    default File getFile(Key key) throws IOException {
        return getFile(key.value());
    }

    File getRaidDirectory();
}
