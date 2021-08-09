package net.forthecrown.valhalla.data.triggers.functions;

import net.forthecrown.serializer.JsonSerializable;

public interface TriggerFunction extends JsonSerializable {
    void execute();
}
