package net.forthecrown.valhalla.data.triggers.functions;

import net.forthecrown.valhalla.active.ActiveRaid;

public interface ParameterizedTriggerFunction<E> extends TriggerFunction {
    void execute(E value, ActiveRaid raid);

    @Override
    default void execute(ActiveRaid raid) {
        throw new UnsupportedOperationException("Unsupported, use execute(E, ActiveRaid)");
    }
}
