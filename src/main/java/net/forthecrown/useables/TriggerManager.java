package net.forthecrown.useables;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.forthecrown.events.Events;
import net.forthecrown.events.TriggerListener;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldVec3i;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongConsumer;

/**
 * A class which manages {@link UsableTrigger} instances.
 *
 * The biggest use of this class is the {@link GlobalTriggerManager} used to
 * manage all triggers created by commands and such.
 */
public class TriggerManager {
    private final Map<String, UsableTrigger> triggers = new Object2ObjectOpenHashMap<>();

    // Look below for an explanation for this map
    private final Map<String, TriggerWorld> worlds = new Object2ObjectOpenHashMap<>();

    @Getter
    private TriggerListener listener;

    public boolean isListenerRegistered() {
        return listener != null;
    }

    public void registerListener() {
        if (isListenerRegistered()) return;

        listener = new TriggerListener(this);
        Events.register(listener);
    }

    public void unregisterListener() {
        if (!isListenerRegistered()) return;

        Events.unregister(listener);
        listener = null;
    }

    /**
     * Adds the given trigger to this manager
     * @param trigger The trigger to add
     * @return True, if trigger was not already contained by this manager
     */
    public boolean add(UsableTrigger trigger) {
        if (triggers.containsKey(trigger.getName())) {
            return false;
        }

        triggers.put(trigger.getName(), trigger);

        TriggerWorld world = worlds.computeIfAbsent(trigger.getArea().getWorld().getName(), s -> new TriggerWorld());
        world.add(trigger);

        return true;
    }

    /**
     * Gets a trigger by its name
     * @param name The name of the trigger
     * @return The found trigger, null, if no trigger was found with the given name
     */
    public UsableTrigger getNamed(String name) {
        return triggers.get(name);
    }

    /**
     * Checks if this manager contains a trigger with the given name
     * @param name The trigger's name
     * @return True, if this manager has the trigger
     */
    public boolean contains(String name) {
        return getNamed(name) != null;
    }

    /**
     * Gets all triggers that overlap the given position
     * @param pos The position
     * @return All triggers at the given position, will just
     *         be an empty list if no triggers overlap the area
     */
    public List<UsableTrigger> getTriggers(WorldVec3i pos) {
        var world = getWorld(pos.getWorld());

        if (world == null || world.isEmpty()) {
            return new ArrayList<>();
        }

        return world.get(pos.x(), pos.y(), pos.z());
    }

    /**
     * Removes the given trigger from this manager
     * @param trigger The trigger to remove
     * @return True, if this manager contained this trigger and it was removed
     */
    public boolean remove(UsableTrigger trigger) {
        if (triggers.remove(trigger.getName()) == null) {
            return false;
        }

        var world = getWorld(trigger.getArea().getWorld());
        world.remove(trigger);

        if (world.isEmpty()) {
            worlds.remove(trigger.getArea().getWorld().getName());
        }

        return true;
    }

    /**
     * Runs the triggers this player overlaps with
     * @param player The player to run triggers for
     */
    public void run(Player player, Location source, Location destination) {
        var w = getWorld(player.getWorld());

        if (w == null || w.isEmpty()) {
            return;
        }

        w.run(player, source, destination);
    }

    /**
     * Clears this manager
     */
    public void clear() {
        triggers.clear();
        worlds.clear();
    }

    /**
     * Saves all the triggers in this manager into a single list
     * @return The NBT representation of all triggers in this manager
     */
    public void save(CompoundTag tag) {
        for (var v: triggers.values()) {
            var triggerTag = new CompoundTag();
            v.save(triggerTag);

            tag.put(v.getName(), triggerTag);
        }
    }

    /**
     * Loads all triggers from the given list
     * @param lTag The list to load from
     */
    public void load(CompoundTag lTag) {
        clear();

        for (var t: lTag.tags.entrySet()) {
            var name = t.getKey();
            var tag = (CompoundTag) t.getValue();

            add(new UsableTrigger(name, tag));
        }
    }

    /**
     * Gets the names of all triggers in this manager
     * @return All trigger names
     */
    public Set<String> getNames() {
        return triggers.keySet();
    }

    /**
     * Gets the size of this manager
     * @return The amount of triggers held by this manager
     */
    public int size() {
        return triggers.size();
    }

    /**
     * Checks if this manager is empty
     * @return True, if {@link #size()} <= 0
     */
    public boolean isEmpty() {
        return triggers.isEmpty();
    }

    private TriggerWorld getWorld(World world) {
        return worlds.get(world.getName());
    }

    /**
     * The way I attempted to speed up spatial trigger
     * lookups is with a system where triggers are placed
     * into these TriggerWorlds where they are additionally
     * placed into lists for each chunk they inhabit, this
     * way we only have to loop through the triggers in a
     * single chunk and not every trigger in the world or
     * on the server when finding triggers to activate.
     * <p>
     * If I cared enough, I would've also divided these
     * chunks along the Y axis for faster lookups, although
     * I fear that would sacrifice memory
     */
    static class TriggerWorld {
        /**
         * The Long here is the chunk position packed into a
         * long If you ever need to modify this, you can use
         * ChunkPos#asLong to turn any x, z coordinate into
         * a packed long, and you can use `new ChunkPos(long)`
         * to get a chunk position from a packed long
         */
        private final Long2ObjectMap<List<UsableTrigger>> triggers = new Long2ObjectOpenHashMap<>();

        void add(UsableTrigger trigger) {
            runForChunks(trigger, pos -> {
                var chunkTriggers = triggers.computeIfAbsent(pos, pos1 -> new ArrayList<>());
                chunkTriggers.add(trigger);
            });
        }

        void remove(UsableTrigger trigger) {
            runForChunks(trigger, (pos) -> {
                List<UsableTrigger> chunkTriggers = triggers.get(pos);

                if (chunkTriggers == null || chunkTriggers.isEmpty()) {
                    return;
                }

                chunkTriggers.remove(trigger);

                if (chunkTriggers.isEmpty()) {
                    triggers.remove(pos);
                }
            });
        }

        // Runs a for loop for each chunk position the trigger is in
        void runForChunks(UsableTrigger trigger, LongConsumer consumer) {
            var area = trigger.getArea();
            var min = Vectors.getChunk(area.min());
            var max = Vectors.getChunk(area.max());

            for (int x = min.x; x <= max.x; x++) {
                for (int z = min.z; z <= max.z; z++) {
                    consumer.accept(ChunkPos.asLong(x, z));
                }
            }
        }

        List<UsableTrigger> get(int x, int y, int z) {
            List<UsableTrigger> triggers = new ArrayList<>();

            ChunkPos pos = new ChunkPos(
                    SectionPos.blockToSectionCoord(x),
                    SectionPos.blockToSectionCoord(z)
            );

            var chunkTriggers = this.triggers.get(pos.toLong());

            if (Util.isNullOrEmpty(chunkTriggers)) {
                return triggers;
            }

            for (var t: chunkTriggers) {
                if (t.getArea().contains(x, y, z)) {
                    triggers.add(t);
                }
            }

            return triggers;
        }

        void run(Player player, Location source, Location destination) {
            Vector3i pos = Vectors.fromI(source);
            Vector3i destPos = Vectors.fromI(destination);
            ChunkPos cPos = Vectors.getChunk(pos);
            ChunkPos dPos = Vectors.getChunk(destPos);

            // If the given chunk positions are different
            // run checks for both, if they're the same
            // only run checks on the destination chunk
            if (!cPos.equals(dPos)) {
                run(cPos, player, pos, destPos);
            }

            run(dPos, player, pos, destPos);
        }

        private void run(ChunkPos cPos, Player player, Vector3i pos, Vector3i dest) {
            var chunkTriggers = this.triggers.get(cPos.toLong());

            if (Util.isNullOrEmpty(chunkTriggers)) {
                return;
            }

            for (var t: chunkTriggers) {
                if (!t.getType().shouldRun(t.getArea(), pos, dest)) {
                    continue;
                }

                t.interact(player);
            }
        }

        boolean isEmpty() {
            return triggers.isEmpty();
        }
    }
}