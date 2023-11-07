package net.forthecrown.cosmetics.travel;

import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.registry.Registry;
import net.forthecrown.menu.Slot;

public class TravelEffects {

  public static final Cosmetic<TravelEffect> SMOKE = Cosmetic.create(
      new SmokeTravelEffect(),
      Slot.of(4, 1).getIndex(),
      "Smoke",
      "Hit that vape, yo ",
      "amirite kids "
  );

  public static final Cosmetic<TravelEffect> HEART = Cosmetic.create(
      new HeartTravelEffect(),
      Slot.of(2, 1).getIndex(),
      "Hearts",
      "Fly in a blaze of love "
  );

  public static final Cosmetic<TravelEffect> PINK_ROCKET = Cosmetic.create(
      new PinkRocketTravelEffect(),
      Slot.of(1, 1).getIndex(),
      "Pink Rocket",
      "Fly to the moon ",
      "with sprinkles :D."
  );

  public static final Cosmetic<TravelEffect> BEAM = Cosmetic.create(
      new BeamTravelEffect(),
      Slot.of(3, 1).getIndex(),
      "Beam",
      "Beam me up, Scotty "
  );

  public static void registerAll(Registry<Cosmetic<TravelEffect>> r) {
    r.register("smoke", SMOKE);
    r.register("hearts", HEART);
    r.register("pink_rocket", PINK_ROCKET);
    r.register("beam", BEAM);
  }
}