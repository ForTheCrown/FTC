package net.forthecrown.challenges;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Interval at which challenges are reset
 */
@Getter
@RequiredArgsConstructor
public enum ResetInterval {
  /**
   * Reset everytime the date changes
   */
  DAILY("Daily") {
    @Override
    public int getMax(ChallengeConfig config) {
      return config.maxDailyChallenges;
    }
  },

  /**
   * Reset on every monday
   */
  WEEKLY("Weekly") {
    @Override
    public int getMax(ChallengeConfig config) {
      return config.maxWeeklyChallenges;
    }
  },

  /**
   * Never automatically reset
   */
  MANUAL("") {
    @Override
    public int getMax(ChallengeConfig config) {
      return -1;
    }

    @Override
    public boolean shouldRefill() {
      return false;
    }
  };

  private final String displayName;

  public abstract int getMax(ChallengeConfig config);

  public boolean shouldRefill() {
    return true;
  }
}