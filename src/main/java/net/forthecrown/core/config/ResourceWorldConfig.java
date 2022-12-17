package net.forthecrown.core.config;

import static net.kyori.adventure.text.Component.text;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.concurrent.TimeUnit;
import jdk.jfr.Timestamp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@ConfigData(filePath = "resource_world.json")
public final class ResourceWorldConfig {

  public static boolean enabled = true;

  public static LongList legalSeeds = new LongArrayList();
  public static String toHazGate = "res_to_haz";
  public static String toResGate = "haz_to_res";
  public static String worldGuardSpawn = "rw_spawn";
  public static String spawnStructure = "rw_spawn";

  public static long sectionRetentionTime = TimeUnit.MINUTES.toMillis(5);
  public static long resetInterval = TimeUnit.DAYS.toMillis(28 * 2);

  public static float doubleDropRate = 0.5F;

  public static Component resetStart = text("The resource world has began resetting!",
      NamedTextColor.YELLOW);
  public static Component resetEnd = text("The resource world has reset!", NamedTextColor.YELLOW);

  @Timestamp
  public static long lastReset;

  public static long lastSeed;

  public static int nextSize = 3000;

  private ResourceWorldConfig() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}