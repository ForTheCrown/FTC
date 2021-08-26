package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;

public enum UserPref implements JsonSerializable {
    FORBIDS_EMOTES,
    FORBIDS_RIDING,
    FORBIDS_TPA,
    FORBIDS_PAY,
    FORBIDS_REGION_INVITES,

    NON_HULK_SMASHER,

    IGNORING_BROADCASTS,
    CANNOT_SWAP_BRANCH,
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
