package net.forthecrown.user.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTitlesTest {

    @Test
    void hasTitle() {
        RankTier t1 = RankTier.TIER_1;
        RankTier t2 = RankTier.TIER_2;
        RankTier t3 = RankTier.TIER_3;

        assertTrue(t1.ordinal() < t2.ordinal());
        assertTrue(t3.ordinal() >= t1.ordinal());
    }
}