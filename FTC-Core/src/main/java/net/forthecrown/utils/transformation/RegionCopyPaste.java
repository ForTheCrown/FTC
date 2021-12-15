package net.forthecrown.utils.transformation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.Vector3iOffset;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A copy and paste of a region from one place to another
 */
public class RegionCopyPaste implements Runnable {
    private final World destinationWorld;
    private final Vector3i destination;
    private final FtcBoundingBox origin;

    private List<CopyPreProcessor> preProcessors = null;
    private List<CopyTransformer> transformers = null;
    private List<BlockFilter> blockFilters = null;

    public RegionCopyPaste(World destinationWorld, Vector3i destination, FtcBoundingBox origin) {
        this.destinationWorld = destinationWorld;
        this.destination = destination;
        this.origin = origin;
    }

    public FtcBoundingBox getOrigin() {
        return origin;
    }

    @Nullable
    public List<CopyPreProcessor> getPreProcessors() {
        return preProcessors;
    }

    @Nullable
    public List<CopyTransformer> getTransformers() {
        return transformers;
    }

    @Nullable
    public List<BlockFilter> getBlockFilters() {
        return blockFilters;
    }

    public RegionCopyPaste addTransformer(CopyTransformer transformer) {
        if(transformers == null) transformers = new ObjectArrayList<>();

        transformers.add(transformer);
        return this;
    }

    public RegionCopyPaste addPreProcessor(CopyPreProcessor processor) {
        if(preProcessors == null) preProcessors = new ObjectArrayList<>();

        preProcessors.add(processor);
        return this;
    }

    public RegionCopyPaste addFilter(BlockFilter filter) {
        if(blockFilters == null) blockFilters = new ObjectArrayList<>();

        blockFilters.add(filter);
        return this;
    }

    public World getDestinationWorld() {
        return destinationWorld;
    }

    public boolean shouldBeSync() {
        int totalArea = origin.sizeX() * origin.sizeY() * origin.sizeZ();
        return totalArea > BoundingBoxes.MIN_MULTI_THREAD;
    }

    @Override
    public void run() {
        if(shouldBeSync()) runSync();
        else runAsync();
    }

    public void runAsync() {
        CompletableFuture.runAsync(runnable(true), BoundingBoxes.COPY_EXECUTOR)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public void runSync() {
        Bukkit.getScheduler().runTask(Crown.inst(), runnable(false));
    }

    private Runnable runnable(boolean async) {
        return () -> {
            Vector3i minPos = origin.getMin();

            for (Block b: origin) {
                // Get the offset of the block in relation
                // to the minimum point of the origin
                Vector3iOffset offset = Vector3iOffset.of(minPos, Vector3i.of(b));

                //The destination of the block in pasting
                WorldVec3i destPos = offset.apply(destinationWorld, destination);

                //Block info
                BlockCopyInfo info = new BlockCopyInfo(b, destPos.getBlock());

                //If filters don't like this block lol
                if(!testBlock(info, async)) continue;

                //PreProcessor -> block pasting -> PostTransformer
                runPreprocessors(info, async);
                info.paste().setBlockData(info.copy().getBlockData().clone(), !async);
                runTransformers(info, async);
            }
        };
    }

    private boolean testBlock(BlockCopyInfo info, boolean async) {
        if(ListUtils.isNullOrEmpty(blockFilters)) return true;

        for (BlockFilter f: blockFilters) {
            if (!f.test(info, this, async)) return false;
        }

        return true;
    }

    private void runPreprocessors(BlockCopyInfo info, boolean async) {
        if(ListUtils.isNullOrEmpty(preProcessors)) return;
        preProcessors.forEach(processor -> processor.process(info, this, async));
    }

    private void runTransformers(BlockCopyInfo info, boolean async) {
        if(ListUtils.isNullOrEmpty(transformers)) return;
        transformers.forEach(transformer -> transformer.transform(info, this, async));
    }
}
