package net.forthecrown.economy.houses;

import net.forthecrown.core.Crown;
import net.forthecrown.economy.houses.components.EnchanterComponent;
import net.forthecrown.registry.Registries;

public final class Houses {
    private Houses() {}

    public static final boolean ENABLED = false;

    public static final House
            KETIL       = register("Ketil"),
            SOMERS      = register("Somers"),
            STEDVOR     = register("Stedvor"),
            FERNIMORE   = register("Fernimore"),
            DAWSEY      = register("Dawsey"),
            LAGGARD     = register("Laggard");

    public static void init() {
        SOMERS.addComponent(new EnchanterComponent());

        Registries.HOUSES.close();

        if(!ENABLED) return;
        HouseSerializer.deserialize();

        /*for (House h: Registries.HOUSES) {
            Crown.getDayUpdate().addListener(h::onDayUpdate);
        }*/

        Crown.logger().info("Houses loaded");
    }

    private static House register(String name) {
        return register(new House(name));
    }

    private static House register(House h) {
        return Registries.HOUSES.register(h.key(), h);
    }
}
