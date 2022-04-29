package net.forthecrown.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;

@RequiredArgsConstructor
public abstract class AbstractUserAttachment
        implements UserAttachment,
        JsonSerializable, JsonDeserializable
{
    @Getter
    protected final FtcUser user;
    public final String serialKey;
}