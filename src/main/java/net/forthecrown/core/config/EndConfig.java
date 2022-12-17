package net.forthecrown.core.config;

import static net.kyori.adventure.text.Component.text;

import net.forthecrown.core.Worlds;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@ConfigData(filePath = "the_end.json")
public final class EndConfig {
  private EndConfig() {}

  private static final WorldVec3i DEFAULT_LEVEL_POS = new WorldVec3i(Worlds.overworld(), 257, 82, 197);

  public static Component openMessage = text("The end is now open!", NamedTextColor.YELLOW);
  public static Component closeMessage = text("The end is now closed!", NamedTextColor.GRAY);

  public static boolean open = false;
  public static boolean enabled = true;
  public static int nextSize = 3000;

  public static WorldVec3i leverPos = DEFAULT_LEVEL_POS;
}