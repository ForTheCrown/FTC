package net.forthecrown.cosmetics.travel;

import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;

public final class TravelEffects {
    private TravelEffects() {}

    private static BuiltInventory INVENTORY;

    public static void init() {
        //Register here with register() method :D

        //Final thing
        Registries.TRAVEL_EFFECTS.close();
        INVENTORY = CosmeticConstants.baseInventory(54, Component.text(""), true)
                .addAll(Registries.TRAVEL_EFFECTS)
                .build();
    }

    private static <T extends TravelEffect> T register(T val) {
        return (T) Registries.TRAVEL_EFFECTS.register(val.key(), val);
    }

    public static BuiltInventory getInventory() {
        return INVENTORY;
    }
}
