package net.forthecrown.protection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectCollections;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class AbstractClaimMap implements ClaimMap {
    protected final Long2ObjectMap<ProtectedClaim> claims = new Long2ObjectOpenHashMap<>();

    @Override
    public void add(ProtectedClaim claim) {
        Validate.isTrue(!contains(claim), "Container already contains claim");
        claims.put(claim.getClaimID(), claim);
    }

    @Override
    public @Nullable ProtectedClaim get(int x, int z) {
        for (ProtectedClaim c: claims.values()) {
            if(!c.getBounds().contains(x, z)) continue;

            return c;
        }

        return null;
    }

    @Override
    public @Nullable ProtectedClaim get(long id) {
        return claims.get(id);
    }

    @Override
    public void remove(long id) {
        Validate.isTrue(contains(id), "Container does not contain given claim: " + id);
        claims.remove(id);
    }

    @Override
    public boolean contains(long id) {
        return claims.containsKey(id);
    }

    @Override
    public void clear() {
        claims.clear();
    }

    @Override
    public int size() {
        return claims.size();
    }

    @Override
    public LongSet getIds() {
        return LongSets.unmodifiable(claims.keySet());
    }

    @Override
    public Collection<ProtectedClaim> getClaims() {
        return ObjectCollections.unmodifiable(claims.values());
    }

    @Override
    public boolean isLegalForNewClaim(Bounds2i b) {
        for (ProtectedClaim c: claims.values()) {
            if(!c.getBounds().overlaps(b)) return false;
        }

        return true;
    }
}
