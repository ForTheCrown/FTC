package net.forthecrown.serializer;

import net.forthecrown.user.FtcUser;

import java.io.File;
import java.util.UUID;

/**
 * Represents an interface to serialize a user
 */
public interface UserSerializer {

    /**
     * Serialize, aka save, the user's data
     * @param user The user to save the data of
     */
    void serialize(FtcUser user);

    /**
     * Deserialize, aka reload, the user's data
     * @param user The user to reload the data of
     */
    void deserialize(FtcUser user);

    /**
     * Code to execute when a user is unloaded
     * @param user The user that's being unloaded
     */
    void onUnload(FtcUser user);

    /**
     * Deletes a certain user's data
     * @param id the user whose data to delete
     */
    void delete(FtcUser id);

    /**
     * Gets the file of a user by the given UUID
     * @param id the id to get the file of
     * @return The user's file
     */
    File getFile(UUID id);
}
