package net.forthecrown.valhalla;

public class RaidDifficulty {
    public static float MAX = 10f;
    public static float MIN = 0.75f;

    private final float modifier;

    public RaidDifficulty(float modifier) {
        this.modifier = modifier;
    }

    public static RaidDifficulty easy() {
        return new RaidDifficulty(MIN);
    }

    public static RaidDifficulty medium() {
        return new RaidDifficulty((MAX - MIN) / 4);
    }

    public static RaidDifficulty hard() {
        return new RaidDifficulty((MAX - MIN) / 2);
    }

    public static RaidDifficulty extreme() {
        return new RaidDifficulty(MAX);
    }

    public float getModifier() {
        return modifier;
    }

    public double multiply(double initial) {
        return initial * (modifier / 2);
    }

    public double addTo(double initial) {
        return initial + (modifier * 2);
    }
}
