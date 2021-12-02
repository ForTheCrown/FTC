package net.forthecrown.user;

/**
 * Represents something that can be attached to a user
 * <p>Examples of this are the UserDataContainer and the UserInteractions</p>
 */
public interface UserAttachment {

    /**
     * Gets the user that holds this attachment
     * @return The attachment holder
     */
    CrownUser getUser();
}
