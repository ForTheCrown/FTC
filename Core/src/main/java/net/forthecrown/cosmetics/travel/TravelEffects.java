package net.forthecrown.cosmetics.travel;

import net.forthecrown.core.Crown;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;

public final class TravelEffects {
    private TravelEffects() {}

    private static BuiltInventory INVENTORY;

    public static TravelEffect SMOKE, HEART, PINK_ROCKET, BEAM;

    public static void init() {
        //Register here with register() method :D
        SMOKE = register(new SmokeTravelEffect());
        HEART = register(new HeartTravelEffect());
        PINK_ROCKET = register(new PinkRocketTravelEffect());
        BEAM = register(new BeamTravelEffect());

        //Final thing
        Registries.TRAVEL_EFFECTS.close();
        INVENTORY = CosmeticConstants.baseInventory(36, Component.text("Region pole effects"), true)
                .addAll(Registries.TRAVEL_EFFECTS)
                .add(CosmeticConstants.NO_TRAVEL)
                .build();

        Crown.logger().info("Travel effects registered");
    }

    private static TravelEffect register(TravelEffect val) {
        return Registries.TRAVEL_EFFECTS.register(val.key(), val);
    }

    public static BuiltInventory getInventory() {
        return INVENTORY;
    }
}
