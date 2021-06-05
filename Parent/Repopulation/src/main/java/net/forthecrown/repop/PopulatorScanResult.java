package net.forthecrown.repop;

import net.forthecrown.emperor.utils.BlockPos;
import net.querz.mca.Chunk;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;

import java.util.List;

public class PopulatorScanResult {
    
    public final List<BlockPos> potentialSpawns;
    public final ListTag<CompoundTag> entities;
    public final CompoundTag structuresTag;
    public final CompoundTag exactStructureTag;
    public final Chunk chunk;

    public PopulatorScanResult(List<BlockPos> potentialSpawns, CompoundTag structuresTag, CompoundTag exactStructureTag, Chunk chunk) {
        this.potentialSpawns = potentialSpawns;
        this.structuresTag = structuresTag;
        this.exactStructureTag = exactStructureTag;
        this.chunk = chunk;
        this.entities = chunk.getEntities();
    }
}
