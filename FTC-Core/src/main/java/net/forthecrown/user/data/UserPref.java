package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;

/**
 * User properties, or preferences.
 * <p></p>
 * Written in a way to keep the property map small
 * and to not serialize values that are default, like
 * how everyone has emotes enabled by default.
 *
 * Well, then there's no need to serialize or store
 * that data if it's the default value
 */
public enum UserPref implements JsonSerializable {
    FORBIDS_EMOTES,
    FORBIDS_RIDING,
    FORBIDS_TPA,
    FORBIDS_PAY,
    FORBIDS_REGION_INVITES,

    NON_HULK_SMASHER,

    IGNORING_BROADCASTS,
    PROFILE_PRIVATE,
    LISTENING_TO_EAVESDROPPER,
    VANISHED,
    GOD_MODE,
    FLYING;

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
