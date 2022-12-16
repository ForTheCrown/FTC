package net.forthecrown.user;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonWrapper;

import java.util.UUID;

import static net.forthecrown.user.UserLookup.NO_NAME_CHANGE;

/**
 * A user's entry in the {@link UserLookup}
 */
@Data
@Setter(AccessLevel.PACKAGE)
public class UserLookupEntry {
    public static final String
            KEY_UUID = "uuid",
            KEY_NAME = "name",
            KEY_NICK = "nick",
            KEY_LAST_NAME = "lastName",
            KEY_NAME_CHANGE = "lastNameChange";

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
    long lastNameChange = NO_NAME_CHANGE;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "index: " + uniqueId +
                ", name: " + name +
                (getNickname() == null ? "" : ", nick: " + nickname) +
                (getLastName() == null ? "" : ", lastname: " + lastName) +
                "}";
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    public JsonElement serialize() {
        var json = JsonWrapper.create();
        json.addUUID(KEY_UUID, getUniqueId());
        json.add(KEY_NAME, getName());

        if (!Strings.isNullOrEmpty(getNickname())) {
            json.add(KEY_NICK, getNickname());
        }

        if (getLastName() != null
                && Time.isPast(GeneralConfig.dataRetentionTime + getLastNameChange())
        ) {
            json.add(KEY_LAST_NAME, getLastName());
            json.addTimeStamp(KEY_NAME_CHANGE, getLastNameChange());
        }

        return json.getSource();
    }

    public static UserLookupEntry deserialize(JsonElement element) {
        var json = JsonWrapper.wrap(element.getAsJsonObject());
        UserLookupEntry entry = new UserLookupEntry(json.getUUID(KEY_UUID));

        entry.setName(json.getString(KEY_NAME));
        entry.setNickname(json.getString(KEY_NICK));
        entry.setLastName(json.getString(KEY_LAST_NAME));
        entry.setLastNameChange(json.getTimeStamp(KEY_NAME_CHANGE));

        if (entry.lastNameChange != NO_NAME_CHANGE
                && Time.isPast(GeneralConfig.dataRetentionTime + entry.lastNameChange)
        ) {
            entry.lastName = null;
            entry.lastNameChange = NO_NAME_CHANGE;
        }

        return entry;
    }
}