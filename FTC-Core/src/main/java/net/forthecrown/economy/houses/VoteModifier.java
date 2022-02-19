package net.forthecrown.economy.houses;

import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.MathUtil;

public class VoteModifier {
    public static final int MAX_VALUE = 50;

    //Negative against, positive pro
    private final byte value;

    public VoteModifier(int value) {
        this.value = (byte) MathUtil.clamp(value, -MAX_VALUE, MAX_VALUE);
    }

    public int getValue() {
        return value;
    }

    public boolean shouldVoteFor() {
        if(MathUtil.inRange(value, -15, 15)) return FtcUtils.RANDOM.nextBoolean();

        return value + FtcUtils.RANDOM.intInRange(-15, 15) > 0;
    }
}
