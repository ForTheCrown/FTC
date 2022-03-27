package net.forthecrown.dungeons.rewrite_4;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.SpawnRequirement;
import net.forthecrown.dungeons.rewrite_4.component.BossStatusListener;
import net.forthecrown.dungeons.rewrite_4.component.SpawnHandler;
import net.forthecrown.dungeons.rewrite_4.type.BossType;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.Locations;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.math.WorldBounds3i;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class DungeonBoss implements Nameable {
    protected final Set<BossComponent> components = new ObjectOpenHashSet<>();
    protected final Set<CompTracker> compTrackers = new ObjectOpenHashSet<>();

    private final BossType type;
    private final BossIdentifier identifier;

    private BossContext currentContext;
    private Location spawnLocation;
    private WorldBounds3i room;
    private SpawnRequirement requirement;

    private boolean alive;

    public DungeonBoss(BossType type) {
        this.type = type;
        this.identifier = BossIdentifier.create(type);
    }

    public DungeonBoss(BossIdentifier identifier) {
        this.type = identifier.type();
        this.identifier = identifier;
    }

    public boolean attemptSpawn(Player player) {
        if (!requirement.test(player)) return false;
        if(alive) return false;

        spawn();
        return true;
    }

    public void spawn() {
        if(alive) return;

        currentContext = BossContext.create(getRoom());

        SpawnHandler handler = getComponent(SpawnHandler.class);
        Validate.notNull(handler, "Dungeon boss " + getName() + " is missing a spawn handler component");

        handler.onSpawn(currentContext);
    }

    public void kill() { kill(false); }
    public void kill(boolean forced) {
        if (!alive) return;

        for (BossStatusListener c: getComponents(BossStatusListener.class)) {
            c.onBossDeath(currentContext, forced);
        }

        currentContext = null;
        alive = false;
    }

    public void save(CompoundTag tag) {
        CompoundTag baseData = new CompoundTag();
        baseData.put("room", room.save());

        baseData.putString("requirement_type", Registries.SPAWN_REQUIREMENTS.getKey(requirement.getType()).asString());
        baseData.put("requirement_data", requirement.save());
        baseData.put("spawn_location", Locations.save(spawnLocation));

        tag.put("base_data", tag);

        for (BossComponent c: components) {
            c.save(tag);
        }
    }

    public void load(CompoundTag tag) {
        CompoundTag baseData = tag.getCompound("base_data");
        room = WorldBounds3i.of(baseData.getCompound("room"));
        spawnLocation = Locations.load(baseData.get("spawn_location"));

        SpawnRequirement.Type type = Registries.SPAWN_REQUIREMENTS.read(tag.get("requirement_type"));
        requirement = type.load(tag.get("requirement_data"));

        for (BossComponent c: components) {
            c.load(tag);
        }
    }

    public BossType getType() {
        return type;
    }

    public void addComponent(BossComponent component) {
        if(components.add(component)) {
            component.setBoss(this);
            component.onBossSet(this);

            for (CompTracker f: compTrackers) {
                if (f.getClazz().isInstance(component)) {
                    f.getComponents().add(component);
                }
            }
        }
    }

    public void addTracker(CompTracker tracker) {
        if(compTrackers.add(tracker)) {
            tracker.setBoss(this);
        }
    }

    public void removeTracker(CompTracker tracker) {
        if(compTrackers.remove(tracker)) {
            tracker.setBoss(null);
        }
    }

    public <T> CompTracker<T> getTracker(Class<T> clazz) {
        for (CompTracker f: compTrackers) {
            if(f.getClazz().equals(clazz)) return f;
        }

        return null;
    }

    public void removeComponent(BossComponent component) {
        if(components.remove(component)) {
            component.setBoss(null);
        }
    }

    public <T> T getComponent(Class<T> clazz) {
        for (BossComponent c: components) {
            if(clazz.isInstance(c)) return (T) c;
        }

        return null;
    }

    public <T> List<T> getComponents(Class<T> clazz) {
        List<T> result = new ObjectArrayList<>();

        for (BossComponent c: components) {
            if(clazz.isInstance(c)) result.add((T) c);
        }

        return result;
    }

    public boolean isAlive() {
        return alive;
    }

    public BossContext getCurrentContext() {
        return currentContext;
    }

    public Location getSpawnLocation() {
        return spawnLocation == null ? null : spawnLocation.clone();
    }

    public World getWorld() {
        return spawnLocation.getWorld();
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public WorldBounds3i getRoom() {
        return room;
    }

    public void setRoom(WorldBounds3i room) {
        this.room = room;
    }

    public SpawnRequirement getRequirement() {
        return requirement;
    }

    public void setRequirement(SpawnRequirement requirement) {
        this.requirement = requirement;
    }

    public BossIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return getType().getName();
    }
}