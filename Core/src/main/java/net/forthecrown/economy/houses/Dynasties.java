package net.forthecrown.economy.houses;

import net.forthecrown.registry.Registries;

public final class Dynasties {
    private Dynasties() {}

    public static final Dynasty KETIL =       register("Ketil");
    public static final Dynasty SOMERS =      register("Somers");
    public static final Dynasty STEDVOR =     register("Stedvor");
    public static final Dynasty FERNIMORE =   register("Fernimore");
    public static final Dynasty DAWSEY =      register("Dawsey");
    public static final Dynasty LAGGARD =     register("Laggard");

    public static void init() {
        Registries.DYNASTIES.close();
        DynastySerializer.deserialize();
    }

    private static Dynasty register(String name) {
        return register(new Dynasty(name));
    }

    private static Dynasty register(Dynasty h) {
        return Registries.DYNASTIES.register(h.key(), h);
    }
}
