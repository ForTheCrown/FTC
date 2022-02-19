package net.forthecrown.economy.houses;

import net.forthecrown.utils.math.MathUtil;

public enum RelationType {
    HORRIBLE (-100, -76),
    BAD (-75, -26),
    NEUTRAL (-25,  25),
    POSITIVE (26, 75),
    GREAT (76, 100);

    private final byte min, max;

    RelationType(int min, int max) {
        this.min = (byte) min;
        this.max = (byte) max;
    }

    public byte getMax() {
        return max;
    }

    public byte getMin() {
        return min;
    }

    public boolean inRange(int amount) {
        return MathUtil.inRange(amount, min, max);
    }

    public boolean isPositive() {
        return min > NEUTRAL.getMax();
    }

    public boolean isNegative() {
        return max < NEUTRAL.getMin();
    }

    public boolean isNeutral() {
        return this == NEUTRAL;
    }

    public RelationType stepWorse() {
        int next = ordinal() - 1;
        if(next < 0) return null;

        return values()[next];
    }

    public RelationType stepBetter() {
        int next = ordinal() + 1;
        if(next > values().length) return null;

        return values()[next];
    }
}
