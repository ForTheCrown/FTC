package net.forthecrown.economy.houses;

import net.forthecrown.registry.Registries;

public final class Houses {
    private Houses() {}

    public static final House KETIL =       register("Ketil");
    public static final House SOMERS =      register("Somers");
    public static final House STEDVOR =     register("Stedvor");
    public static final House FERNIMORE =   register("Fernimore");
    public static final House DAWSEY =      register("Dawsey");
    public static final House LAGGARD =     register("Laggard");

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
