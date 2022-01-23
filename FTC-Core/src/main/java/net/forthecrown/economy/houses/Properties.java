package net.forthecrown.economy.houses;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;

import java.util.Map;

public final class Properties {
    private Properties() {}

    static final Map<String, Property> NAME_2_PROPERTY = new Object2ObjectOpenHashMap<>();

    public static final Property.FloatProperty
            SUPPLY_RECOVERY_RATE    = register(new Property.FloatProperty("supplyRecoveryRate", 0.1F)),
            MAX_DEMAND_CHANGE       = register(new Property.FloatProperty("maxDemandChange", 0.2F));

    private static <T extends Property> T register(T prop) {
        NAME_2_PROPERTY.put(prop.name, prop);
        return prop;
    }

    public static Property get(String name) {
        return NAME_2_PROPERTY.get(name);
    }


    public static void init() {
        //Empty initializer method
        Crown.logger().info("House Properties initialized");
    }
}