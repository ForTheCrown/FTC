package net.forthecrown.valhalla.data.triggers.functions;

import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.valhalla.active.ActiveRaid;

public interface ParameterizedTriggerFunction<E> extends JsonSerializable {
    void execute(E value, ActiveRaid raid);
}
