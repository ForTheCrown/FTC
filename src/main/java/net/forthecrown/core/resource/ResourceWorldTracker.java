package net.forthecrown.core.resource;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.core.config.ResourceWorldConfig;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.utils.math.Vectors;
import org.apache.logging.log4j.Logger;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.math.vector.Vector3i;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RequiredArgsConstructor
public class ResourceWorldTracker implements SerializableObject {
    private static final Logger LOGGER = FTC.getLogger();

    /** File format used by sections, custom */
    private static final String FORMAT_SUFFIX = ".non_natural";

    private static final ResourceWorldTracker inst = new ResourceWorldTracker();

    /** Map of region position to region */
    private final Map<McRegionPos, WorldRegion> nonNaturalBySection = new Object2ObjectOpenHashMap<>();

    /** Section directory */
    @Getter
    private final Path directory;

    private ResourceWorldTracker() {
        this.directory = PathUtil.getPluginDirectory("rw_data");
    }

    /* ----------------------------- INSTANCES ------------------------------ */

    public static ResourceWorldTracker get() {
        return inst;
    }

    /* ----------------------------- SAVE / RELOAD FOR ALL SECTIONS ------------------------------ */

    @Override @OnSave
    public void save() {
        for (var e: nonNaturalBySection.entrySet()) {
            try {
                saveSection(e.getKey(), e.getValue());
            } catch (IOException exc) {
                LOGGER.error("Failed to serialize section: '{}'", e.getKey(), exc);
            }
        }
    }

    @Override
    public void reload() {
        // This is only ever called by staff,
        // so I see no reason to actually attempt
        // re-loading every region in memory
        clear();
    }

    /* ----------------------------- QUERYING AND MODIFICATION ------------------------------ */

    public void setNonNatural(Vector3i pos) {
        McRegionPos regionPos = McRegionPos.of(pos.x(), pos.z());
        WorldRegion region = getOrCreate(regionPos);

        // Only null if there was an error loading or creating the file
        if (region == null) {
            return;
        }

        long blockPos = toLong(pos);
        region.nonNatural.add(blockPos);
        region.dirty = true;

        // Block was broken, player is active, don't unload
        // section just yet
        pushbackTask(regionPos, region);
    }

    public boolean isNatural(Vector3i pos) {
        McRegionPos regionPos = McRegionPos.of(pos.x(), pos.z());
        Either<WorldRegion, Boolean> regionQuery = get(regionPos);

        // Right being present means either error or no region
        // If region failed to load, this will return false
        // otherwise true, as that means no region file exists,
        // meaning no blocks have been broken
        if (regionQuery.right().isPresent()) {
            return regionQuery.right().get();
        }

        WorldRegion region = regionQuery.left().get();
        long blockPos = toLong(pos);

        // Pushback here because these calls should only
        // be made via event listener, thus if the
        // method is called now, it's likely it'll be
        // called again later, so pushback the expiry now
        pushbackTask(regionPos, region);
        return !region.nonNatural.contains(blockPos);
    }

    private long toLong(Vector3i pos) {
        return Vectors.toLong(pos);
    }

    public void clear() {
        for (var r: nonNaturalBySection.values()) {
            Tasks.cancel(r.unloadTask);
        }

        nonNaturalBySection.clear();
    }

    public void reset() {
        clear();

        PathUtil.safeDelete(getDirectory(), true, true)
                .resultOrPartial(LOGGER::error)
                .ifPresent(integer -> LOGGER.info("Purged {} section files", integer));
    }

    /* ----------------------------- SECTION MANAGEMENT ------------------------------ */

    private Either<WorldRegion, Boolean> get(McRegionPos pos) {
        var loaded = nonNaturalBySection.get(pos);

        if (loaded != null) {
            return Either.left(loaded);
        }

        if (!Files.exists(getFile(pos))) {
            return Either.right(true);
        }

        try {
            WorldRegion region = new WorldRegion();
            loadSection(pos, region);

            nonNaturalBySection.put(pos, region);
            return Either.left(region);
        } catch (IOException e) {
            LOGGER.error("Couldn't read section: '" + pos + "'", e);
            return Either.right(false);
        }
    }

    private WorldRegion getOrCreate(McRegionPos pos) {
        var loaded = nonNaturalBySection.get(pos);

        if (loaded != null) {
            return loaded;
        }

        try {
            WorldRegion region = new WorldRegion();

            if (Files.exists(getFile(pos))) {
                loadSection(pos, region);
            } else {
                region.nonNatural = new LongOpenHashSet(200);
            }

            nonNaturalBySection.put(pos, region);
            return region;
        } catch (IOException e) {
            LOGGER.error("Couldn't load world region: '" + pos + "'", e);
        }

        return null;
    }

    private void pushbackTask(McRegionPos pos, WorldRegion region) {
        Tasks.cancel(region.unloadTask);

        region.unloadTask = Tasks.runLaterAsync(
                () -> unloadSection(pos),
                Time.millisToTicks(ResourceWorldConfig.sectionRetentionTime)
        );
    }

    private void unloadSection(McRegionPos pos) {
        var section = nonNaturalBySection.remove(pos);

        if (section == null) {
            return;
        }

        try {
            saveSection(pos, section);
            LOGGER.info("Unloaded RW section {}", pos);
        } catch (IOException e) {
            LOGGER.error("Couldn't save section: '{}'", pos, e);
        }

        Tasks.cancel(section.unloadTask);
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    private void saveSection(McRegionPos pos, WorldRegion region) throws IOException {
        Path sectionFile = getFile(pos);

        // Don't serialize empty regions and regions
        // that don't need to be serialized
        if (region.nonNatural == null
                || region.nonNatural.isEmpty()
                || !region.dirty
        ) {
            return;
        }

        var output = Files.newOutputStream(sectionFile);
        DataOutputStream stream = new DataOutputStream(output);

        writeSection(stream, region);

        stream.close();
        output.close();

        region.dirty = false;

        if (FTC.inDebugMode()) {
            LOGGER.info("Saved section {}", pos);
        }
    }

    private void loadSection(McRegionPos pos, WorldRegion region) throws IOException {
        Path sectionFile = getFile(pos);

        if (!Files.exists(sectionFile)) {
            throw new IOException(
                    "Section file '" + sectionFile.getFileName() + "' does not exist, cannot load"
            );
        }

        InputStream inputStream = Files.newInputStream(sectionFile);
        DataInputStream dataInput = new DataInputStream(inputStream);

        readSection(dataInput, region);

        dataInput.close();
        inputStream.close();
    }

    private Path getFile(McRegionPos pos) {
        return directory.resolve(pos.x() + "_" + pos.y() + FORMAT_SUFFIX);
    }

    // --- READING AND WRITING SECTIONS ---
    // Note:
    // Since these files only have 1 and ONLY 1 purpose,
    // that being to store data about which blocks have
    // been broken in the resource world, the file format
    // used is immensely simple, a binary format where the
    // first 4 bytes of a file is the amount of positions
    // stored within it, and the rest of the bytes are
    // simply the longs themselves
    //
    // The longs themselves are simply packed coordinates,
    // see toLong(Vector3i) for how x y z coordinates get
    // packed to a single long
    //     -- Jules <3

    private void writeSection(DataOutput output, WorldRegion region) throws IOException {
        output.writeInt(region.nonNatural.size());

        for (long l: region.nonNatural) {
            output.writeLong(l);
        }
    }

    private void readSection(DataInput input, WorldRegion region) throws IOException {
        int expected = input.readInt();
        region.nonNatural = new LongOpenHashSet(expected);

        for (int i = 0; i < expected; i++) {
            region.nonNatural.add(input.readLong());
        }
    }

    /**
     * A simple record to represent the position of a region file
     * region, I'm great with words.
     * <p>
     * Block -> region pos conversion is done with the {@link #of(int, int)}
     * method, which just bit shifts the given block coordinates 7
     * times to the right, way faster than dividing lol
     */
    record McRegionPos(int x, int y) {
        static McRegionPos of(int blockX, int blockZ) {
            return new McRegionPos(blockX >> 7, blockZ >> 7);
        }
    }

    /**
     * The object which represents a single 8x8 chunk
     * area, aka a 128x128 block area that the RW is
     * subdivided into to allow for faster and less
     * memory-intense lookups and insertions.
     * <p>
     * Contains only 3 things, a long set, each long in
     * the set being a packed block position, {@link #toLong(Vector3i)},
     * and the other being to unload task of this region.
     * <p>
     * The final thing is a 'dirty' boolean, if it's set
     * to true it means the regions has been changed and
     * is waiting to be serialized, if false, no changes
     * have been made to the region, and it doesn't need
     * to be serialized. This is to prevent the unneeded
     * saving of regions
     */
    static class WorldRegion {
        /** True, if region has unsaved changes, false otherwise */
        boolean dirty = false;

        /** Set of non-natural block positions */
        LongSet nonNatural;

        BukkitTask unloadTask;
    }
}