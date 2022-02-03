package net.forthecrown.protection;

import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.forthecrown.utils.math.ImmutableVector3i;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

public interface ClaimMap extends Iterable<ProtectedClaim> {

    void add(ProtectedClaim claim);

    @Nullable
    ProtectedClaim get(long id);

    @Nullable
    ProtectedClaim get(int x, int z);

    @Nullable
    default ProtectedClaim get(Location l) {
        return get(l.getBlockX(), l.getBlockZ());
    }

    @Nullable
    default ProtectedClaim get(BlockVector2 vec2) {
        return get(vec2.getX(), vec2.getZ());
    }

    @Nullable
    default ProtectedClaim get(ImmutableVector3i v) {
        return get(v.getX(), v.getZ());
    }

    default boolean isEmptyPos(int x, int y) {
        return get(x, y) == null;
    }

    void remove(long id);

    default void remove(ProtectedClaim claim) {
        remove(claim.getClaimID());
    }

    boolean contains(long id);

    default boolean contains(ProtectedClaim claim) {
        return contains(claim.getClaimID());
    }

    boolean isLegalForNewClaim(Bounds2i b);

    int size();
    void clear();

    void save(CompoundTag tag);
    void load(CompoundTag tag);

    LongSet getIds();
    Collection<ProtectedClaim> getClaims();

    default boolean isEmpty() {
        return size() < 0;
    }

    @NotNull
    @Override
    default Iterator<ProtectedClaim> iterator() {
        return getClaims().iterator();
    }
}
