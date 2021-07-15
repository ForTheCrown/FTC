package net.forthecrown.valhalla;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.math.FtcRegion;
import net.forthecrown.valhalla.data.VikingRaid;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Valhalla implements Namespaced {
    public static final String VIKING_NAMESPACE = "vikings";
    public static final KeyArgument KEY_PARSER = KeyArgument.key(VIKING_NAMESPACE);
    private static Valhalla instance;

    private final Registry<VikingRaid> loadedRaids;
    private final Set<Key> existingRaids = new HashSet<>();

    private final VikingRaidSerializer serializer;
    private boolean activeRaidExists;

    private Valhalla() {
        this.loadedRaids = Registries.create("viking_raids");
        this.serializer = new VikingRaidSerializer();

        findExistingRaids();
    }

    public static void init() {
        instance = new Valhalla();
    }

    public static Valhalla getInstance() {
        return instance;
    }

    private void findExistingRaids() {
        for (String s: serializer.getRaidDirectory().list()) {
            try {
                existingRaids.add(KEY_PARSER.parse(new StringReader(s)));
            } catch (CommandSyntaxException ignored) {}
        }
    }

    public void saveAll() {
        loadedRaids.forEach(serializer::serialize);
    }

    public VikingRaid getRaid(Key key) {
        if(!existingRaids.contains(key)) return null;
        if(loadedRaids.contains(key)) return loadedRaids.get(key);

        try {
            VikingRaid raid = serializer.deserialize(key);
            loadedRaids.register(key, raid);

            return raid;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public VikingRaid addRaid(Key key, Location startLoc, FtcRegion region) {
        Position pos = new PositionImpl(startLoc.getX(), startLoc.getY(), startLoc.getZ());
        BoundingBox box = BoundingBox.of(region.getMax(), region.getMin());

        VikingRaid raid = new VikingRaid(key, box, pos);
        loadedRaids.register(key, raid);
        existingRaids.add(key);

        return raid;
    }

    public Registry<VikingRaid> getLoadedRaids() {
        return loadedRaids;
    }

    public Set<Key> getExistingRaids() {
        return existingRaids;
    }

    public RaidSerializer getSerializer() {
        return serializer;
    }

    public void setActiveRaidExists(boolean activeRaidExists) {
        this.activeRaidExists = activeRaidExists;
    }

    public boolean activeRaidExists() {
        return activeRaidExists;
    }

    @Override
    public @NotNull String namespace() {
        return VIKING_NAMESPACE;
    }
}
