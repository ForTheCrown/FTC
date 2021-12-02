package net.forthecrown.user;

import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;

public abstract class AbstractUserAttachment
        implements UserAttachment,
        JsonSerializable, JsonDeserializable
{
    protected final FtcUser user;

    public AbstractUserAttachment(FtcUser user) {
        this.user = user;
    }

    @Override
    public CrownUser getUser() {
        return user;
    }
}
