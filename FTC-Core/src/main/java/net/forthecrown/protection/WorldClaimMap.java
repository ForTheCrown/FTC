package net.forthecrown.protection;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollections;
import net.forthecrown.core.Crown;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class WorldClaimMap implements ClaimMap {
    private final ClaimsImpl claims;
    private final World world;

    private final Long2ObjectMap<ProtectedClaim> worldClaims = new Long2ObjectOpenHashMap<>();
    private final Map<ClaimPos, SectionClaimMap> sections = new Object2ObjectOpenHashMap<>();
    boolean starting;

    public WorldClaimMap(ClaimsImpl claims, World world) {
        this.world = world;
        this.claims = claims;
        starting = true;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void add(ProtectedClaim claim) {
        Validate.isTrue(!contains(claim), "Container already contains claim with ID: " + claim.getClaimID());

        worldClaims.put(claim.getClaimID(), claim);
        claims.getTakenIDs().add(claim.getClaimID());

        for (ClaimPos p: ClaimUtil.findApplicableSections(claim.getBounds())) {
            SectionClaimMap container = sections.computeIfAbsent(p, SectionClaimMap::new);
            container.add(claim);
        }

        dropEmptySections();
    }

    void dropEmptySections() {
        Iterator<Map.Entry<ClaimPos, SectionClaimMap>> iterator = sections.entrySet().iterator();

        while (iterator.hasNext()) {
            SectionClaimMap map = iterator.next().getValue();
            if(map == null || map.isEmpty()) iterator.remove();
        }
    }

    @Override
    public @Nullable ProtectedClaim get(int x, int z) {
        ClaimPos pos = ClaimPos.ofAbsolute(x, z);
        ClaimMap map = getSection(pos);
        if(map == null || map.isEmpty()) return null;

        return map.get(x, z);
    }

    @Override
    public @Nullable ProtectedClaim get(Location l) {
        if(!l.getWorld().equals(world)) return null;
        return get(l.getBlockX(), l.getBlockZ());
    }

    @Override
    public void remove(long id) {
        ProtectedClaim claim = get(id);
        Validate.notNull(claim, "Container does not contain claim: " + id);

        worldClaims.remove(id);
        claims.getTakenIDs().remove(id);

        for (ClaimPos p: ClaimUtil.findApplicableSections(claim.getBounds())) {
            ClaimMap map = getSection(p);
            if(map == null || map.isEmpty()) continue;

            map.remove(claim);
        }

        dropEmptySections();
    }

    public void resize(ProtectedClaim claim, Bounds2i newBounds) {
        remove(claim);
        claim.setBounds(newBounds);
        add(claim);
    }

    @Override
    public boolean contains(long id) {
        return worldClaims.containsKey(id);
    }

    @Override
    public boolean isLegalForNewClaim(Bounds2i b) {
        for (ClaimPos p: ClaimUtil.findApplicableSections(b)) {
            ClaimMap section = getSection(p);
            if(section == null) continue;

            if(!section.isLegalForNewClaim(b)) return false;
        }

        return true;
    }

    public ClaimMap getSection(ClaimPos pos) {
        return sections.get(pos);
    }

    public ProtectedClaim get(long id) {
        return worldClaims.get(id);
    }

    @Override
    public int size() {
        return worldClaims.size();
    }

    @Override
    public void clear() {
        worldClaims.clear();
        sections.clear();
    }

    @Override
    public void save(CompoundTag tag) {
        for (ProtectedClaim c: worldClaims.values()) {
            CompoundTag cTag = new CompoundTag();
            c.save(cTag);

            tag.put(c.getClaimID() + "", cTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        LongSet notLoaded = new LongOpenHashSet(worldClaims.keySet());

        for (Map.Entry<String, Tag> e: tag.tags.entrySet()) {
            CompoundTag t = (CompoundTag) e.getValue();
            long id = Long.parseLong(e.getKey());

            ProtectedClaim c = get(id);

            if(c == null) {
                if(!starting) Crown.logger().warn("Found unknown region ID: {} while loading claims for {}, creating new claim", id, world.getName());

                c = new ProtectedClaim(id);
                c.load(t);
                add(c);
            } else {
                c.load(t);
            }

            notLoaded.remove(id);
        }

        if(!notLoaded.isEmpty()) {
            Crown.logger().warn("Found unserialized claims: {}", notLoaded);
            Crown.logger().warn("Removing not loaded claims");

            for (long l: notLoaded) {
                remove(get(l));
            }
        }
    }

    @Override
    public LongSet getIds() {
        return LongSets.unmodifiable(worldClaims.keySet());
    }

    @Override
    public Collection<ProtectedClaim> getClaims() {
        return ObjectCollections.unmodifiable(worldClaims.values());
    }
}
