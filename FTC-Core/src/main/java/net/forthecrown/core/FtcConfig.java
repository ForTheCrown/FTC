package net.forthecrown.core;

import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

/**
 * Class representing the configuration.json file
 */
public interface FtcConfig extends CrownSerializer {

    /**
     * Gets the prefix FTC should be using
     * @return The FTC prefix, '[FTC] '
     */
    Component prefix();

    /**
     * Gets the JSON behind the config
     * @return config's JSON
     */
    JsonWrapper getJson();

    /**
     * Gets the server spawn location.
     * This is the location everyone will spawn at
     * when they join the server for the first time
     *
     * @return The server spawn
     */
    Location getServerSpawn();

    /**
     * Sets the server spawn location.
     * @see FtcConfig#getServerSpawn()
     *
     * @param location The spawn location
     */
    void setServerSpawn(Location location);

    /**
     * Adds a section to the config
     * @param attachment The section to add
     */
    void addSection(ConfigSection attachment);

    /**
     * Removes a section from the config
     * @param attachment The section to remove
     */
    void removeSection(ConfigSection attachment);

    /**
     * A config section is just an object which is serialized and deserialized to and from the
     * FtcConfig. It must provide a serializationKey, aka just a key for the json object so
     * the config can find it.
     */
    abstract class ConfigSection implements JsonSerializable, JsonDeserializable {
        public final String serializationKey;

        public ConfigSection(String serializationKey) {
            this.serializationKey = serializationKey;
        }
    }
}
