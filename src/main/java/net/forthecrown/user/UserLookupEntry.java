package net.forthecrown.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.UUID;

/**
 * A user's entry in the {@link UserLookup}
 */
@Data
@Setter(AccessLevel.PACKAGE)
public class UserLookupEntry {
    /**
     * The user's ID
     */
    final UUID uniqueId;

    /**
     * The current name of the user
     */
    String name;

    /**
     * The user's nickname, may be null
     */
    String nickname;

    /**
     * The user's last name
     */
    String lastName;

    /**
     * The timestamp of when the user last changed
     * their name
     */
    long lastNameChange = UserLookup.NO_NAME_CHANGE;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "index: " + uniqueId +
                ", name: " + name +
                (getNickname() == null ? "" : ", nick: " + nickname) +
                (getLastName() == null ? "" : ", lastname: " + lastName) +
                "}";
    }
}