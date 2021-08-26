package net.forthecrown.valhalla.data.triggers.functions;

import net.forthecrown.serializer.SerializationTypeHolder;
import net.forthecrown.valhalla.active.ActiveRaid;

public interface TriggerFunction extends SerializationTypeHolder<TriggerFunction> {
    void execute(ActiveRaid raid);
}
