package net.forthecrown.core.config;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.Worlds;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.kyori.adventure.text.Component.text;

@ConfigData(filePath = "the_end.json")
public @UtilityClass class EndConfig {
    private static transient final WorldVec3i DEFAULT_LEVEL_POS = new WorldVec3i(Worlds.overworld(), 257, 82, 197);

    public Component openMessage = text("The end is now open!", NamedTextColor.YELLOW);
    public Component closeMessage = text("The end is now closed!", NamedTextColor.GRAY);

    public boolean open    = false;
    public boolean enabled = true;
    public int nextSize    = 3000;

    public WorldVec3i leverPos = DEFAULT_LEVEL_POS;
}