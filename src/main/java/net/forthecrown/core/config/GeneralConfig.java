package net.forthecrown.core.config;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Set;

@ConfigData(filePath = "config.json")
public @UtilityClass class GeneralConfig {
    @Setter
    private Location serverSpawn;

    public Component prefix;

    private Set<String> illegalWorlds = new ObjectOpenHashSet<>();

    public Location getServerSpawn() {
        return serverSpawn == null ? null : serverSpawn.clone();
    }

    public boolean isIllegalWorld(World world) {
        return illegalWorlds.contains(world.getName());
    }

    public void addIllegalWorld(World world) {
        illegalWorlds.add(world.getName());
    }

    public void removeIllegalWorld(World world) {
        illegalWorlds.remove(world.getName());
    }
}