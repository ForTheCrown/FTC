package net.forthecrown.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.structure.BlockPlacer;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.StructurePlaceContext;
import net.forthecrown.useables.warps.Warp;
import net.forthecrown.utils.Bukkit2NMS;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.utils.world.WorldReCreator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.world.level.border.WorldBorder;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mcteam.ancientgates.Gate;
import org.mcteam.ancientgates.Gates;

import java.util.ArrayList;

public class ResourceWorld extends FtcConfig.ConfigSection implements DayChangeListener {
    private static final Logger LOGGER = Crown.logger();

    private final LongList legalSeeds = new LongArrayList();
    private String toHazGate, toResGate;
    private Component resetStart, resetEnd;
    private long lastReset;
    private int size;
    private boolean autoResetEnabled;
    private Key spawnStructure;

    ResourceWorld() {
        super("resource_world");
    }

    public void resetAndLoad() {
        if(!Registries.STRUCTURES.contains(spawnStructure)) {
            LOGGER.error("Cannot start RW reset, no spawn structure with id '{}' found", spawnStructure);
            return;
        }

        LOGGER.info("Starting RW reset");

        World original = Worlds.resource();

        // Kick players out and close gates
        kickPlayers(original);
        setGatesStatus(false);

        // Attempt to announce closing
        if(resetStart == null) {
            LOGGER.warn("resetStart message is null, cannot announce");
        } else {
            Crown.getAnnouncer().announce(resetStart);
        }

        // Re-create world
        WorldReCreator creator = WorldReCreator.of(original)
                .seed(findSeed())
                .preserveGameRules(true)
                .preserveWorldBorder(true);

        World newWorld = creator.run();
        WorldBorder border = Bukkit2NMS.getLevel(newWorld).getWorldBorder();
        border.setSize(getSize());

        WorldLoader.loadAsync(newWorld)
                .whenComplete((world, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("Could not regen Resource World", throwable);
                        return;
                    }

                    BlockStructure spawn = Registries.STRUCTURES.get(spawnStructure);
                    if (spawn == null) {
                        LOGGER.error("Cannot find RW spawn, no structure with key {} found", spawnStructure);
                        return;
                    }

                    int y = world.getHighestBlockYAt(0, 0);

                    StructurePlaceContext context = new StructurePlaceContext(
                            spawn,
                            new Vector3i(0, y, 0),
                            BlockPlacer.world(world)
                    )
                            .placeEntities(true)
                            .addEmptyEntityProcessor()
                            .addEmptyProcessor();

                    // Run rest of the code in sync with the server
                    Bukkit.getScheduler().runTask(Crown.inst(), () -> {
                        // Place spawn structure
                        spawn.place(context);

                        // Move gates' locations
                        Location gateLocation = new Location(world, -6, y + 1, 0, -90, 0);
                        Location destination = new Location(world, 0, y + 2, 0, 90, 0);

                        Gate toHaz = Gate.get(toHazGate);
                        Gate toRes = Gate.get(toResGate);

                        // Move Haz -> RW gate from position
                        if (toHaz == null) {
                            LOGGER.warn("Could not re-position rw -> haz gate, could not find gate with id {}", toHazGate);
                        } else {
                            // First null forces the 'from' list to be
                            // emptied, second method call actually sets
                            // the 'from' location. Don't ask me why it's
                            // like this, this was in /setfrom's code
                            toHaz.addFrom(null);
                            toHaz.addFrom(gateLocation);
                        }

                        // Move RW -> Haz gate destination
                        if (toRes == null) {
                            LOGGER.warn("Could not re-position rw -> haz gate, could not find gate with id {}", toResGate);
                        } else {
                            // Same deal with the 2 method calls as above
                            toRes.addTo(null);
                            toRes.addTo(destination);
                        }

                        // Move portal warp
                        Key key = Keys.forthecrown("portal");
                        Warp portalWarp = Crown.getWarpManager().get(key);

                        if (portalWarp == null) {
                            LOGGER.warn("Could not find portal warp, no warp with id {}", key);
                        } else {
                            portalWarp.setDestination(destination);
                        }

                        // Attempt to announce the end's reset being finished
                        if (resetEnd == null) {
                            LOGGER.warn("resetEnd message is null, cannot announce opening");
                        } else {
                            Crown.getAnnouncer().announce(resetEnd);
                        }

                        // Open gates
                        setGatesStatus(true);
                        LOGGER.info("RW reset finished");
                    });
                });
    }

    private void kickPlayers(World w) {
        Location hazel = FtcUtils.findHazelLocation();

        for (Player p: w.getPlayers()) {
            p.teleport(hazel);
        }
    }

    private void setGatesStatus(boolean open) {
        String status = open ? "open" : "close";
        Gate g = Gate.get(toHazGate);

        if(g != null) {
            if(open) Gates.open(g);
            else Gates.close(g);
        } else {
            LOGGER.warn("Cannot {} haz -> res gate, no gate with {} ID found", status, toHazGate);
        }

        g = Gate.get(toResGate);

        if(g != null) {
            if(open) Gates.open(g);
            else Gates.close(g);
        } else {
            LOGGER.warn("Cannot {} res -> haz gate, no gate with {} ID found", status, toHazGate);
        }

        Gate.save();
    }

    @Override
    public void onDayChange() {
        if(!isAutoResetEnabled()) return;

        final long nextReset = lastReset + ComVars.resourceWorldResetInterval();
        if (!TimeUtil.isPast(nextReset)) return;

        resetAndLoad();
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        this.autoResetEnabled = json.getBool("enabled");
        this.lastReset = json.getLong("last_reset");
        setSize(json.getInt("size"));

        legalSeeds.clear();
        if(json.has("legal_seeds")) {
            this.legalSeeds.addAll(json.getList("legal_seeds", JsonElement::getAsLong, new ArrayList<>()));
        }

        this.spawnStructure = json.getKey("spawn_structure");

        this.toHazGate = json.getString("rw_to_haz_gate");
        this.toResGate = json.getString("haz_to_rw_gate");

        this.resetStart = json.getComponent("reset_start_message");
        this.resetEnd = json.getComponent("reset_end_message");
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("enabled", autoResetEnabled);
        json.add("last_reset", lastReset);
        json.add("size", size);

        if(!legalSeeds.isEmpty()) {
            json.addList("legal_seeds", legalSeeds, JsonPrimitive::new);
        }

        json.addKey("spawn_structure", spawnStructure);

        json.add("rw_to_haz_gate", toHazGate);
        json.add("haz_to_rw_gate", toResGate);

        json.addComponent("reset_start_message", resetStart);
        json.addComponent("reset_end_message", resetEnd);

        return json.getSource();
    }

    private long findSeed() {
        if(legalSeeds.isEmpty()) return FtcUtils.RANDOM.nextLong();
        return FtcUtils.RANDOM.pickRandomEntry(legalSeeds);
    }

    public Component getResetStart() {
        return resetStart;
    }

    public void setResetStart(Component resetStart) {
        this.resetStart = resetStart;
    }

    public Component getResetEnd() {
        return resetEnd;
    }

    public void setResetEnd(Component resetEnd) {
        this.resetEnd = resetEnd;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;

        World w = Worlds.resource();
        w.getWorldBorder().setSize(size);
    }

    public boolean isAutoResetEnabled() {
        return autoResetEnabled;
    }

    public void setAutoResetEnabled(boolean autoResetEnabled) {
        this.autoResetEnabled = autoResetEnabled;
    }

    public long getLastReset() {
        return lastReset;
    }

    public LongList getLegalSeeds() {
        return legalSeeds;
    }

    public String getToHazGate() {
        return toHazGate;
    }

    public void setToHazGate(String toHazGate) {
        this.toHazGate = toHazGate;
    }

    public String getToResGate() {
        return toResGate;
    }

    public void setToResGate(String toResGate) {
        this.toResGate = toResGate;
    }

    public Key getSpawnStructure() {
        return spawnStructure;
    }

    public void setSpawnStructure(Key spawnStructure) {
        this.spawnStructure = spawnStructure;
    }
}
