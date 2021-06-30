package net.forthecrown.serializer;

/**
 * An abstract class to make file serialization and deserialization easier
 */
public interface CrownSerializer {

    /**
     * Saves the file
     */
    void save();

    /**
     * Reloads the file
     */
    void reload();
}
