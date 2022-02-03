package net.forthecrown.protection;

import net.minecraft.nbt.CompoundTag;

public class SectionClaimMap extends AbstractClaimMap {
    private final ClaimPos pos;

    public SectionClaimMap(ClaimPos pos) {
        this.pos = pos;
    }

    public ClaimPos getPos() {
        return pos;
    }

    @Override
    public void save(CompoundTag tag) {

    }

    @Override
    public void load(CompoundTag tag) {

    }
}