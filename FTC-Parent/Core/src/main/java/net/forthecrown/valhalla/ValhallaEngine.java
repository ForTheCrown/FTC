package net.forthecrown.valhalla;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.forthecrown.valhalla.data.VikingRaid;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ValhallaEngine implements Valhalla {
    static ValhallaEngine instance;

    private final Registry<VikingRaid> loadedRaids;
    private final Set<Key> existingRaids = new HashSet<>();
    private final VikingRaidSerializer serializer;

    private ActiveRaid activeRaid;

    private ValhallaEngine() {
        this.loadedRaids = Registries.create("viking_raids");
        this.serializer = new VikingRaidSerializer();

        findExistingRaids();
    }

    public static void init() {
        instance = new ValhallaEngine();
    }

    public static void shutDown() {
        if(instance == null) return;

        if(instance.activeRaidExists()) instance.getActiveRaid().shutDown();
    }

    private void findExistingRaids() {
        for (String s: serializer.getRaidDirectory().list()) {
            try {
                existingRaids.add(KEY_PARSER.parse(new StringReader(s.replaceAll(".json", ""))));
            } catch (CommandSyntaxException ignored) {}
        }
    }

    @Override
    public void saveAll() {
        loadedRaids.forEach(serializer::serialize);
    }

    @Override
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

    @Override
    public VikingRaid addRaid(Key key, Location startLoc, Region region) {
        Position pos = new PositionImpl(startLoc.getX(), startLoc.getY(), startLoc.getZ());

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        BoundingBox box = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());

        VikingRaid raid = new VikingRaid(key, box, pos);
        loadedRaids.register(key, raid);
        existingRaids.add(key);

        return raid;
    }

    @Override
    public Registry<VikingRaid> getLoadedRaids() {
        return loadedRaids;
    }

    @Override
    public Set<Key> getExistingRaids() {
        return existingRaids;
    }

    @Override
    public RaidSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void setActiveRaid(ActiveRaid activeRaid) {
        this.activeRaid = activeRaid;
    }

    @Override
    public ActiveRaid getActiveRaid() {
        return activeRaid;
    }
}
