package net.forthecrown.utils.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.TimePrinter;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Class for loading and pre-generating worlds
 */
public class WorldLoader {
    private static final Logger LOGGER = Crown.logger();

    public static boolean VERBOSE = false;
    public static int LOG_INTERVAL = 100; //How many chunks should be loaded between each progress log

    private static final int
            SECTION_SIZE        = 50, // Size of a single load section, in chunks
            MAX_LOADING_CHUNKS  = 25; // The max chunks we can be loading at any time

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setUncaughtExceptionHandler((t1, e) -> LOGGER.error("Error in LoaderInstance", e));
        return t;
    });

    //World key 2 task
    private static final Map<NamespacedKey, LoaderInstance> ONGOING = new Object2ObjectOpenHashMap<>();

    /**
     * Loads a world asynchronously.
     * @param world The world to load
     *
     * @return A completable future that will be complete once
     *         the entire world has finished loading
     */
    public static CompletableFuture<World> loadAsync(World world) {
        Validate.isTrue(!ONGOING.containsKey(world.getKey()), "World is already being loaded");

        LoaderInstance instance = new LoaderInstance(world);
        EXECUTOR.execute(instance);

        return instance.result;
    }

    public static void stopLoading(World world) {
        LoaderInstance instance = ONGOING.get(world.getKey());
        Validate.notNull(instance, "World is not loading");
        ONGOING.remove(world.getKey());

        instance.onCancel();
    }

    private static void complete(World world) {
        ONGOING.remove(world.getKey());
    }

    /**
     * Shuts down the loader.
     * <p></p>
     * ONLY TO BE USED BY onDisable IN MAIN
     */
    public static void shutdown() {
        for (LoaderInstance i: ONGOING.values()) {
            i.onCancel();
        }

        ONGOING.clear();
        EXECUTOR.shutdownNow();
    }

    // FUCK THIS BULLSHIT
    // aSyNc cHuNk lOaDiNg MY FUCKING ASS
    // ALL THEY're DOING IS GIVING YOU A COMPLETABLE FUTURE THAT RUNS
    // *ON THE MAIN THREAD* WHAT'S THE FUCKING POINT IN THAT CASE, YOU'RE
    // NOT MOVING THE ISSUE TO A DIFFERENT THREAD, YOU'RE JUST MOVING IT
    // DOWN THE FUCKING LINE, WHO ACTUALLY THOUGHT THAT WAS A GOOD IDEA,
    // FUCKING WHO, I WANT TO SMASH THEIR FACE IN WITH A HAMMER. GOD FUCKING
    // DAMN IT
    // ...
    // STOP TELLING ME TO CENSOR MYSELF INTELLIJ
    //
    // This works, it's the best I can do without it causing tons of errors.
    // I hate Notch, but I can't blame him for not knowing this side project
    // of his would blow up into something massive that NEEDS MORE THAN 1
    // FUCKING THREAD TO RUN.

    // Load progress class used for tracking and logging the progress of
    // a world load.
    private static class LoadProgress {
        private final World world;
        private final long chunkCount;
        private final long started = System.currentTimeMillis();

        private long loaded;
        private long lastLog;

        public LoadProgress(World world, int chunkCount) {
            this.world = world;
            this.chunkCount = chunkCount;
        }

        void onChunkLoaded() {
            lastLog++;
            loaded++;

            if(lastLog >= LOG_INTERVAL) {
                lastLog = 0;
                float progressPercent = (float) loaded / (float) chunkCount * 100;

                LOGGER.info("[{}] Loading progress: is {} / {}, or {}%", world.getName(), loaded, chunkCount, String.format("%.2f", progressPercent));
            }
        }

        void onFinish() {
            long interval = System.currentTimeMillis() - started;
            long msPerChunk = interval / loaded;

            LOGGER.info("[{}] Finished load: took {} or {}ms, average of {} / chunk",
                    world.getName(),
                    new TimePrinter(interval).printString(),
                    interval,
                    new TimePrinter(msPerChunk).printString()
            );
        }
    }

    private static class LoaderInstance implements Runnable {
        private final CompletableFuture<World> result = new CompletableFuture<>();
        private final ExecutorService executor;
        private final LoadSection[] sections;
        private final Semaphore semaphore;
        private final LoadProgress progress;

        private final CraftWorld world;
        private boolean cancelled;

        public LoaderInstance(World world) {
            this.world = (CraftWorld) world;
            this.semaphore = new Semaphore(MAX_LOADING_CHUNKS);

            // Figure out how much of the world has to be loaded based on
            // the size of the world border, only load chunks within world
            // border
            WorldBorder border = world.getWorldBorder();
            int chunkSize = SectionPos.blockToSectionCoord((int) (border.getSize() / 2));
            this.progress = new LoadProgress(world, (chunkSize * 2) * (chunkSize * 2));

            Validate.isTrue(chunkSize >= SECTION_SIZE, "Chunk size cannot be less than " + SECTION_SIZE);

            // Offset position to correct for negative cords
            ChunkPos start = new ChunkPos(-chunkSize, -chunkSize);

            // This uses a system of LoadSections, which takes the
            // area the loader has to load and splits it between
            // square sections called a LoadSection, its size is
            // determined by SECTION_SIZE. All sections run their
            // loading at the same time in parallel. The created
            // semaphore ensures that these sections don't overwhelm
            // the CPU and RAM with chunk load requests

            // Figure out how many load sections we'll be using
            float val = ((float) chunkSize * 2) / ((float) SECTION_SIZE);
            int sectionedSize = (int) Math.max(1, Math.ceil(val));
            this.sections = new LoadSection[sectionedSize * sectionedSize];

            // Create the load sections
            for (int i = 0; i < sections.length; i++) {
                int x = i / sectionedSize;
                int z = i % sectionedSize;

                final int
                        xOffset = x * SECTION_SIZE,
                        zOffset = z * SECTION_SIZE;

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
            for (LoadSection s: sections) {
                if(!s.completed) return;
            }

            progress.onFinish();
            executor.shutdownNow();
            complete(world);

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
            this.cancelled = true;
            executor.shutdownNow();
        }
    }

    private static class LoadSection implements Runnable {
        private final ChunkPos start;
        private final LoaderInstance loader;

        private boolean completed;

        public LoadSection(LoaderInstance loader, ChunkPos start) {
            this.start = start;
            this.loader = loader;
        }

        @Override
        public void run() {
            for (int x = 0; x < SECTION_SIZE; x++) {
                for (int z = 0; z < SECTION_SIZE; z++) {
                    if(loader.cancelled) return;
                    ChunkPos p = new ChunkPos(start.x + x, start.z + z);

                    // In the case that we're loading a massive world
                    // we can't be loading too many chunks at once, that'll
                    // cause CPU and RAM issues, so use the semaphore to
                    // ensure we're not loading too many chunks at once
                    loader.semaphore.acquireUninterruptibly();

                    loader.world.getChunkAtAsync(p.x, p.z)
                                    .whenComplete((chunk, throwable) -> {
                                        if(throwable != null) {
                                            LOGGER.error("Error while loading chunk", throwable);
                                            return;
                                        }

                                        // Logging every chunk normally clutters the console
                                        // So only do it if we're testing
                                        if(VERBOSE) {
                                            LOGGER.info("Loaded chunk [" + p.x + " " + p.z + "]");
                                        }

                                        // Unload the chunk to make sure it doesn't stay in RAM
                                        chunk.unload(true);

                                        //Update progress tracker and release the semaphore permit
                                        loader.progress.onChunkLoaded();
                                        loader.semaphore.release();
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
            return getClass().getSimpleName() + "{start=" + start + ", end=" + new ChunkPos(start.x + SECTION_SIZE, start.z + SECTION_SIZE).toString() + "}";
        }
    }
}
