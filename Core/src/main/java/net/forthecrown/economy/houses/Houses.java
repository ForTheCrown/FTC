package net.forthecrown.economy.houses;

import net.forthecrown.registry.Registries;

public final class Houses {
    private Houses() {}

    public static final House
            KETIL       = register("Ketil"),
            SOMERS      = register("Somers"),
            STEDVOR     = register("Stedvor"),
            FERNIMORE   = register("Fernimore"),
            DAWSEY      = register("Dawsey"),
            LAGGARD     = register("Laggard");

    public static void init() {
        Registries.HOUSES.close();
        HouseSerializer.deserialize();
    }

    private static House register(String name) {
        return register(new House(name));
    }

    private static House register(House h) {
        return Registries.HOUSES.register(h.key(), h);
    }
}
