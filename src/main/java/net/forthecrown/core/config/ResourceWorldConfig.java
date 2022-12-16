package net.forthecrown.core.config;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import jdk.jfr.Timestamp;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;

@ConfigData(filePath = "resource_world.json")
public @UtilityClass class ResourceWorldConfig {
    public boolean enabled = true;

    public LongList legalSeeds = new LongArrayList();
    public String toHazGate = "res_to_haz";
    public String toResGate = "haz_to_res";
    public String worldGuardSpawn = "rw_spawn";
    public String spawnStructure = "rw_spawn";

    public long sectionRetentionTime = TimeUnit.MINUTES.toMillis(5);
    public long resetInterval = TimeUnit.DAYS.toMillis(28 * 2);

    public float doubleDropRate = 0.5F;

    public Component resetStart = text("The resource world has began resetting!", NamedTextColor.YELLOW);
    public Component resetEnd = text("The resource world has reset!", NamedTextColor.YELLOW);

    @Timestamp
    public long lastReset;

    public long lastSeed;

    public int nextSize = 3000;
}