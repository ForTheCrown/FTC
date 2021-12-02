package net.forthecrown.economy.houses;

import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.math.MathUtil;

public class VoteModifier {
    public static final int MAX_VALUE = 50;

    //Negative against, positive pro
    private final byte value;
    private final CrownRandom random = new CrownRandom();

    public VoteModifier(int value) {
        this.value = (byte) MathUtil.clamp(value, -MAX_VALUE, MAX_VALUE);
    }

    public int getValue() {
        return value;
    }

    public boolean shouldVoteFor() {
        if(MathUtil.isInRange(value, -15, 15)) return random.nextBoolean();

        return value + random.intInRange(-15, 15) > 0;
    }
}
