package net.forthecrown;

import net.minecraft.server.level.ServerLevel;

public final class McConstants {
  private McConstants() {}

  public static final int MAX_Y = 312;

  public static final int MIN_Y = -64;

  public static final int Y_SIZE = MAX_Y - MIN_Y;

  public static final int TICKS_PER_DAY = 24_000;
}