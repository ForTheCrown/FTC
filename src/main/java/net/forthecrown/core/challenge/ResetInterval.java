package net.forthecrown.core.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Interval at which challenges are reset */
@Getter
@RequiredArgsConstructor
public enum ResetInterval {
    /** Reset everytime the date changes */
    DAILY ("Daily") {
        @Override
        public int getMax() {
            return ChallengeConfig.maxDailyChallenges;
        }
    },

    /** Reset on every monday */
    WEEKLY ("Weekly") {
        @Override
        public int getMax() {
            return ChallengeConfig.maxWeeklyChallenges;
        }
    },

    /** Never automatically reset */
    MANUAL ("") {
        @Override
        public int getMax() {
            return -1;
        }

        @Override
        public boolean shouldRefill() {
            return false;
        }
    };

    private final String displayName;

    public abstract int getMax();

    public boolean shouldRefill() {
        return true;
    }
}