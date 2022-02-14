package net.forthecrown.math;

public enum Rot {
    D_0 (0),
    D_90 (90),
    D_180 (180),
    D_270 (270);

    final int degrees;

    Rot(int d) {
        this.degrees = d;
    }

    public int getDegrees() {
        return degrees;
    }

    public Rot add(Rot other) {
        return add0(other.ordinal());
    }

    public Rot subtract(Rot other) {
        return add0(-other.ordinal());
    }

    private Rot add0(int add) {
        int newOrdinal = ordinal() + add;
        Rot[] values = values();

        // If added ordinals are within value bounds
        // return given value
        if(newOrdinal >= 0 && newOrdinal < values.length) {
            return values[newOrdinal];
        }

        // If not within bounds check which side it goes over
        // and return corresponding value
        if(newOrdinal < 0) return values[newOrdinal + values.length];
        else return values[newOrdinal - values.length];
    }
}
