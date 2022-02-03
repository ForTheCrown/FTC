package net.forthecrown.protection;

import net.minecraft.nbt.CompoundTag;

public class SubClaimMap extends AbstractClaimMap {
    private final ProtectedClaim claim;

    public SubClaimMap(ProtectedClaim claim) {
        this.claim = claim;
    }

    @Override
    public void add(ProtectedClaim claim) {
        super.add(claim);
        claim.setParent(getClaim());
    }

    @Override
    public void remove(long id) {
        ProtectedClaim claim = get(id);

        if(claim != null) {
            claim.setParent(null);
        }

        super.remove(id);
    }

    public ProtectedClaim getClaim() {
        return claim;
    }

    @Override
    public void save(CompoundTag tag) {

    }

    @Override
    public void load(CompoundTag tag) {

    }
}
