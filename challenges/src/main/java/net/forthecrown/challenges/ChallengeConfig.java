package net.forthecrown.challenges;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ChallengeConfig {

  public int maxDailyChallenges = 5;
  public int maxWeeklyChallenges = 4;
  public int maxStreak = 366;

  public boolean allowRepeatingChallenges = false;
}