package net.forthecrown.valhalla.data.triggers.functions;

import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.SerializerType;

public class TriggerFunctions {
    public static void init() {
        register(BreakBlocksFunction.TYPE);
        register(EntityFunction.TYPE);
        register(PlayAnimationFunction.TYPE);

        Registries.V_FUNCTIONS.close();
    }

    public static void register(SerializerType<? extends TriggerFunction> serializerType) {
        Registries.V_FUNCTIONS.register(serializerType.key(), serializerType);
    }
}
