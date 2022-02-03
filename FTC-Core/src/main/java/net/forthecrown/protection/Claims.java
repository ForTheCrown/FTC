package net.forthecrown.protection;

import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Claims extends CrownSerializer {
    void setAllowsClaims(World world, boolean state);
    boolean allowsClaims(World world);
    WorldClaimMap getClaimMap(World world);

    ProtectedClaim get(long id);
    ProtectedClaim get(World world, int x, int z);

    default ProtectedClaim get(Location l) {
        return get(l.getWorld(), l.getBlockX(), l.getBlockZ());
    }

    default ProtectedClaim get(WorldVec3i v) {
        return get(v.getWorld(), v.getX(), v.getZ());
    }

    ProtectedClaim createClaim(World world, BlockVector2 v1, BlockVector2 v2, @Nullable UUID creator, ClaimType type);

    default ProtectedClaim createAdminClaim(World world, BlockVector2 v1, BlockVector2 v2) {
        return createClaim(world, v1, v2, null, ClaimType.ADMIN);
    }

    void trust(ProtectedClaim claim, UUID trustGiver, UUID trustee, TrustLevel level);
    void untrust(ProtectedClaim claim, UUID trustGiver, UUID uuid);

    LongSet getTakenIDs();
    long generateID();
}
