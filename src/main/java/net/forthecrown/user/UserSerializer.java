package net.forthecrown.user;

import java.util.UUID;

/**
 * Represents an interface to serialize a user.
 * <p>
 * This exists so the format/way a user object is
 * serialized can change without much interference
 * to the rest of the user system. Perfect if, for
 * example, we wanted to switch to serializing users
 * in a database instead of in json files in a server
 * directory
 */
public interface UserSerializer {

    /**
     * Serialize, aka save, the user's data
     * @param user The user to save the data of
     */
    void serialize(User user);

    /**
     * Deserialize, aka reload, the user's data
     * @param user The user to reload the data of
     */
    void deserialize(User user);

    /**
     * Deletes a certain user's data
     * @param id the user whose data to delete
     */
    void delete(UUID id);
}