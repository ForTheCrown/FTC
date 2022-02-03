package net.forthecrown.user;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.protection.ProtectedClaim;

import java.util.Set;

public interface UserClaims extends UserAttachment {
    void add(long id);

    default void add(ProtectedClaim claim) {
        add(claim.getClaimID());
    }

    void remove(long id);

    default void remove(ProtectedClaim claim) {
        remove(claim.getClaimID());
    }

    boolean contains(long id);

    default boolean contains(ProtectedClaim claim) {
        return contains(claim.getClaimID());
    }

    int size();

    boolean isEmpty();

    LongSet getOwned();

    default Set<ProtectedClaim> getOwnedClaims() {
        Set<ProtectedClaim> result = new ObjectOpenHashSet<>();
        return result;
    }
}
