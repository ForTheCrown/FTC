package net.forthecrown.protection;

import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Worlds;
import net.forthecrown.serializer.AbstractNbtSerializer;
import net.forthecrown.utils.ListUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ClaimsImpl extends AbstractNbtSerializer implements Claims {
    private final LongSet takenIds = new LongOpenHashSet();
    private final Random idGenerator = new Random();

    private final Set<NamespacedKey> legalWorlds = new ObjectOpenHashSet<>();
    private final Map<World, WorldClaimMap> claims = new Object2ObjectOpenHashMap<>();

    public ClaimsImpl() {
        super("claims");

        reload();

        claims.forEach((world, map) -> map.starting = false);
    }

    @Override
    public void setAllowsClaims(World world, boolean allows) {
        if(allows) legalWorlds.add(world.getKey());
        else legalWorlds.remove(world.getKey());
    }

    @Override
    public boolean allowsClaims(World world) {
        return legalWorlds.contains(world.getKey());
    }

    @Override
    public WorldClaimMap getClaimMap(World world) {
        if(!allowsClaims(world)) return null;
        return getClaimMap0(world);
    }

    private WorldClaimMap getClaimMap0(World world) {
        return claims.computeIfAbsent(world, w -> new WorldClaimMap(this, w));
    }

    @Override
    public ProtectedClaim get(long id) {
        for (WorldClaimMap m: claims.values()) {
            ProtectedClaim claim = m.get(id);
            if(claim == null) continue;
            return claim;
        }

        return null;
    }

    @Override
    public ProtectedClaim get(World world, int x, int z) {
        Validate.isTrue(allowsClaims(world), "World doesn't allow claims");
        WorldClaimMap m = getClaimMap(world);

        return m.get(x, z);
    }

    @Override
    public ProtectedClaim createClaim(World world, BlockVector2 v1, BlockVector2 v2, @Nullable UUID creator, ClaimType type) {
        Validate.isTrue(allowsClaims(world), "World doesn't allow claims");

        if(type == ClaimType.NORMAL && creator == null) {
            throw new IllegalStateException("Type cannot be normal and have a null creator");
        }

        WorldClaimMap m = getClaimMap(world);
        ProtectedClaim claim = new ProtectedClaim(generateID());
        claim.setOwner(creator);
        claim.setType(type);
        claim.setBounds(Bounds2i.of(v1, v2));

        m.add(claim);

        return claim;
    }

    @Override
    public void trust(ProtectedClaim claim, UUID trustGiver, UUID trustee, TrustLevel level) {

    }

    @Override
    public void untrust(ProtectedClaim claim, UUID trustGiver, UUID uuid) {

    }

    @Override
    public LongSet getTakenIDs() {
        return takenIds;
    }

    @Override
    public long generateID() {
        long result = idGenerator.nextLong();

        byte safeGuard = 127;
        while (takenIds.contains(result)) {
            result = idGenerator.nextLong();
            safeGuard--;

            if(safeGuard <= 0) {
                throw new IllegalStateException("Could not generate claim ID");
            }
        }

        return result;
    }

    @Override
    public void save(CompoundTag tag) {
        ListTag allowed = new ListTag();

        for (NamespacedKey k: legalWorlds) {
            allowed.add(StringTag.valueOf(k.asString()));
        }

        tag.put("legal_worlds", allowed);

        CompoundTag worlds = new CompoundTag();

        for (WorldClaimMap m: claims.values()) {
            CompoundTag worldTag = new CompoundTag();
            m.save(worldTag);

            worlds.put(m.getWorld().getName(), worldTag);
        }

        tag.put("worlds", worlds);
    }

    @Override
    protected void reload(CompoundTag tag) {
        legalWorlds.clear();
        ListTag list = tag.getList("legal_worlds", Tag.TAG_STRING);

        for (Tag t: list) {
            legalWorlds.add(Keys.parse(t.getAsString()));
        }

        if(tag.contains("worlds")) {
            Set<World> unread = new ObjectOpenHashSet<>(claims.keySet());
            CompoundTag worlds = tag.getCompound("worlds");

            for (Map.Entry<String, Tag> t: worlds.tags.entrySet()) {
                World world = Bukkit.getWorld(t.getKey());
                CompoundTag worldTag = (CompoundTag) t.getValue();

                WorldClaimMap map = getClaimMap0(world);
                map.load(worldTag);
                unread.remove(world);
            }

            if(!unread.isEmpty()) {
                Crown.logger().warn("Claims NBT did not contain the following worlds which were in memory: ");
                Crown.logger().warn(ListUtils.join(unread, ", ", "[ ", " ]", World::getName));
                Crown.logger().warn("Removing un-deserialized worlds");

                unread.forEach(claims::remove);
            }
        } else claims.clear();
    }

    @Override
    protected void addDefaults(CompoundTag tag) {
        ListTag list = new ListTag();
        list.add(StringTag.valueOf(Worlds.OVERWORLD_KEY.asString()));
        list.add(StringTag.valueOf(Worlds.NETHER_KEY.asString()));
        list.add(StringTag.valueOf(Worlds.END_KEY.asString()));

        tag.put("legal_worlds", list);
    }
}
