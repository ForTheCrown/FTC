package net.forthecrown.utils.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.Crown;
import net.forthecrown.text.format.PeriodFormat;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Class for loading and pre-generating worlds.
 * <p>
 * This uses a system of LoadSections, which takes the
 * area the loader has to load and splits it between
 * square sections called a LoadSection, its size is
 * determined by SECTION_SIZE. All sections run their
 * loading at the same time in parallel. The created
 * semaphore ensures that these sections don't overwhelm
 * the CPU and RAM with chunk load requests
 */
public @UtilityClass class WorldLoader {
    private final Logger LOGGER = Crown.logger();

    /**
     * Determines if the loader should log an
     * excessive amount of info or not
     */
    public boolean VERBOSE = false;

    /**
     * The amount of chunks that have to be loaded before
     * the logger logs a progress update message
     */
    public int LOG_INTERVAL = 100;

    /**
     * The size of a {@link LoadSection} in chunks
     */
    private final int SECTION_SIZE        = 50;

    /**
     * The max chunks we can be loading at any time
     */
    private final int MAX_LOADING_CHUNKS  = 20;

    /**
     * The maximum world size that can be loaded, anything
     * above this takes too long and most likely generates
     * a world file too large.
     */
    public final double MAX_LOADABLE_AREA = 50_000D;

    /**
     * The semaphore that ensures we never load more than
     * {@link #MAX_LOADING_CHUNKS} at a time.
     * <p>
     * If we passed this limit, there's potential for the server's
     * main thread to die or freeze to a point where it can't
     * continue, so we need to limit ourselves.
     */
    private final Semaphore SEMAPHORE = new Semaphore(MAX_LOADING_CHUNKS);

    /**
     * The executor service used to run world loads
     */
    private final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setUncaughtExceptionHandler((t1, e) -> LOGGER.error("Error in LoaderInstance", e));
        return t;
    });

    /**
     * The loader's world load tracker. It maps all loaded worlds by their world
     * key to the instance of the loader currently running on that world.
     * <p>
     * Tracking required if we'd need to shut down or otherwise interact
     * with any in-progress loader instance.
     */
    private final Map<NamespacedKey, LoaderInstance> ONGOING = new Object2ObjectOpenHashMap<>();

    /**
     * Loads a world asynchronously.
     * @param world The world to load
     *
     * @return A completable future that will be complete once
     *         the entire world has finished loading
     */
    public CompletableFuture<World> loadAsync(World world) {
        // Ensure the world is not already being loaded
        Validate.isTrue(!isLoading(world), "World is already being loaded");

        // Ensure world load area is smaller than the max
        Validate.isTrue(
                world.getWorldBorder().getSize() <= MAX_LOADABLE_AREA,
                "World '%s' size bigger than max load area: %.2f",
                world.getName(), MAX_LOADABLE_AREA
        );

        LoaderInstance instance = new LoaderInstance(world);
        ONGOING.put(world.getKey(), instance);

        EXECUTOR.execute(instance);

        return instance.result;
    }

    /**
     * Stops the give world from being loaded
     * @param world The world being loaded
     */
    public void stopLoading(World world) {
        LoaderInstance instance = ONGOING.get(world.getKey());
        Validate.notNull(instance, "World is not loading");
        ONGOING.remove(world.getKey());

        instance.onCancel();
    }


    // Method called in LoaderInstance to signal the
    // given world having its loading be completed
    private void complete(World world) {
        ONGOING.remove(world.getKey());
    }

    /**
     * Checks if the given world is being loaded
     * @param world The world to check
     * @return True, if world is being loaded, false otherwise
     */
    public boolean isLoading(World world) {
        return ONGOING.containsKey(world.getKey());
    }

    /**
     * Shuts down the loader.
     * <p></p>
     * ONLY TO BE USED BY onDisable IN MAIN
     */
    public void shutdown() {
        // Close all loader instances
        for (LoaderInstance i: ONGOING.values()) {
            i.onCancel();
        }

        // Clear map and shutdown executor
        ONGOING.clear();
        EXECUTOR.shutdownNow();
    }

    /**
     * Load progress class used for tracking and logging the progress of
     * a world load.
     */
    private class LoadProgress {
        private final World world;

        /** Amount of chunks that have need to be loaded */
        private final long chunkCount;

        /** Scan start time stamp */
        private final long started = System.currentTimeMillis();

        /** Amount of loaded chunks */
        private long loaded;

        /**
         * The amount of chunks that the
         * last log had
         */
        private long lastLog;

        public LoadProgress(World world, int chunkCount) {
            this.world = world;
            this.chunkCount = chunkCount;
        }

        void onChunkLoaded() {
            lastLog++;
            loaded++;

            if (lastLog >= LOG_INTERVAL) {
                lastLog = 0;
                float progressPercent = (float) loaded / (float) chunkCount * 100;

                LOGGER.info("[{}] Loading progress: {} / {}, or {}%",
                        world.getName(),
                        loaded,
                        chunkCount,
                        String.format("%.2f", progressPercent)
                );
            }
        }

        void onFinish() {
            long interval = System.currentTimeMillis() - started;
            long msPerChunk = interval / loaded;

            LOGGER.info("[{}] Finished load: took {} or {}ms, average of {} / chunk",
                    world.getName(),
                    PeriodFormat.of(interval),
                    interval,
                    PeriodFormat.of(msPerChunk)
            );
        }
    }

    /**
     * A single instance of a world in the loader.
     */
    private class LoaderInstance implements Runnable {
        private final CompletableFuture<World> result = new CompletableFuture<>();
        private final ExecutorService executor;
        private final LoadSection[] sections;
        private final LoadProgress progress;

        private final CraftWorld world;
        private boolean stopped;

        public LoaderInstance(World world) {
            this.world = (CraftWorld) world;

            // Figure out how much of the world has to be loaded based on
            // the size of the world border, only load chunks within world
            // border
            WorldBorder border = world.getWorldBorder();
            int chunkSize = SectionPos.blockToSectionCoord((int) (border.getSize() / 2));
            this.progress = new LoadProgress(world, (chunkSize * 2) * (chunkSize * 2));

            // Offset position to correct for negative cords
            ChunkPos start = new ChunkPos(-chunkSize, -chunkSize);

            // Figure out how many load sections we'll be using
            float val = ((float) chunkSize * 2) / ((float) SECTION_SIZE);
            int sectionedSize = (int) Math.max(1, Math.ceil(val));
            this.sections = new LoadSection[sectionedSize * sectionedSize];

            // Create the load sections
            for (int i = 0; i < sections.length; i++) {
                // Find X and Z from 1D array, google
                // this I don't know math
                int x = i / sectionedSize;
                int z = i % sectionedSize;

                final int
                        xOffset = x * SECTION_SIZE,
                        zOffset = z * SECTION_SIZE;

                // Set the section to start at the calculated values
                // Since the section's size is a constant, we don't
                // need to specify that.
                sections[i] = new LoadSection(this, new ChunkPos(start.x + xOffset, start.z + zOffset));
            }

            // Create the executor which will handle the load sections
            this.executor = Executors.newFixedThreadPool(sectionedSize, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setUncaughtExceptionHandler((t1, e) -> LOGGER.error("Error in LoadSection", e));

                return t;
            });

            // Log load data
            LOGGER.info(world.getName() + " load data:");
            LOGGER.info("LoadSections used: " + sections.length);
            LOGGER.info("chunkSize: " + chunkSize);
            LOGGER.info("startPos: " + start);
        }

        // Method called by sections to invoke a
        // check if all sections are done or not
        // if all have finished, complete the
        // CompletableFuture with null
        synchronized void completeSection() {
            // Check if all sections have been loaded
            // If any have not been loaded, stop
            for (LoadSection s: sections) {
                if(!s.completed) return;
            }

            // Shutdown this loader instance
            onCancel();
            progress.onFinish();
            complete(world);

            // Complete the result callback
            result.complete(world);
        }

        @Override
        public void run() {
            LOGGER.info("Started load of world: " + world.getName());

            // Start all sections
            for (LoadSection s: sections) {
                executor.execute(s);
            }
        }

        public void onCancel() {
            // Set stopped to true and kill the executor
            this.stopped = true;
            executor.shutdownNow();
        }
    }

    /**
     * A single section of a world that's being loaded
     * by the {@link LoaderInstance}
     */
    private class LoadSection implements Runnable {
        private final ChunkPos start;
        private final LoaderInstance loader;

        private boolean completed;

        public LoadSection(LoaderInstance loader, ChunkPos start) {
            this.start = start;
            this.loader = loader;
        }

        @Override
        public void run() {
            // Run a nested for loop through all of these sections'
            // chunks and load those
            for (int x = 0; x < SECTION_SIZE; x++) {
                for (int z = 0; z < SECTION_SIZE; z++) {
                    // If the loader has been stopped, stop
                    if (loader.stopped) {
                        return;
                    }

                    // The position of the chunk we're loading
                    ChunkPos p = new ChunkPos(start.x + x, start.z + z);

                    // In the case that we're loading a massive world
                    // we can't be loading too many chunks at once, that'll
                    // cause CPU and RAM issues, so use the semaphore to
                    // ensure we're not loading too many chunks at once
                    SEMAPHORE.acquireUninterruptibly();

                    // Load chunk, are these comments obvious enough
                    loader.world.getChunkAtAsync(p.x, p.z, true, false)
                                    .whenComplete((chunk, throwable) -> {
                                        if (throwable != null) {
                                            LOGGER.error("Error while loading chunk", throwable);
                                            return;
                                        }

                                        // Logging every chunk normally clutters the console
                                        // So only do it if we're testing
                                        if (VERBOSE) {
                                            LOGGER.info("Loaded chunk [" + p.x + " " + p.z + "]");
                                        }

                                        // Unload the chunk to make sure it doesn't stay in RAM
                                        // We're just generating the chunks beforehand, not
                                        // trying to overwhelm the server here
                                        if (!Bukkit.isPrimaryThread()) {
                                            VanillaAccess.getServer().execute(() -> chunk.unload(true));
                                        }

                                        //Update progress tracker and release the semaphore permit
                                        loader.progress.onChunkLoaded();
                                        SEMAPHORE.release();
                                    });
                }
            }

            complete();
        }

        private void complete() {
            completed = true;
            loader.completeSection();
            LOGGER.info("Completed " + this);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{start=" + start + ", end=" + new ChunkPos(start.x + SECTION_SIZE, start.z + SECTION_SIZE) + "}";
        }
    }
}