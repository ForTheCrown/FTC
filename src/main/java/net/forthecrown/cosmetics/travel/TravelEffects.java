package net.forthecrown.cosmetics.travel;

public final class TravelEffects {
    private TravelEffects() {}

    public static final TravelEffect
            SMOKE           = new SmokeTravelEffect(),
            HEART           = new HeartTravelEffect(),
            PINK_ROCKET     = new PinkRocketTravelEffect(),
            BEAM            = new BeamTravelEffect();
}