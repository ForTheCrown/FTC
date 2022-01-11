package net.forthecrown.user;

import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;

public abstract class AbstractUserAttachment
        implements UserAttachment,
        JsonSerializable, JsonDeserializable
{
    protected final FtcUser user;
    public final String serialKey;

    public AbstractUserAttachment(FtcUser user, String serialKey) {
        this.user = user;
        this.serialKey = serialKey;
    }

    @Override
    public CrownUser getUser() {
        return user;
    }
}
