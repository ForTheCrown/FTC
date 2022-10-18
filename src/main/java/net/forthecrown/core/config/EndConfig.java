package net.forthecrown.core.config;

import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.kyori.adventure.text.Component.text;

@ConfigData(filePath = "the_end.json")
public class EndConfig {
    public static Component openMessage = text("The end is now open!", NamedTextColor.YELLOW);
    public static Component closeMessage = text("The end is now closed!", NamedTextColor.GRAY);

    public static boolean open = false;
    public static boolean enabled = true;
    public static int nextSize = 3000;

    public static WorldVec3i leverPos;
}