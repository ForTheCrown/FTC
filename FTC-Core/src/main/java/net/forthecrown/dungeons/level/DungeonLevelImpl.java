package net.forthecrown.dungeons.level;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.key.Key;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DungeonLevelImpl implements DungeonLevel {
    private final Key key;

    private String name;
    private World world;
    private Bounds3i bounds;
    private final Long2ObjectMap<BaseSpawner> spawners = new Long2ObjectOpenHashMap<>();
    private Map<Player, SpawnerView> viewers;

    public DungeonLevelImpl(Key key) {
        this.key = key;
    }

    @Override
    public void save(CompoundTag tag) {
        if(spawners.isEmpty()) {
            CompoundTag spawners = new CompoundTag();
            for (Long2ObjectMap.Entry<BaseSpawner> e: getSpawners().long2ObjectEntrySet()) {
                spawners.put(e.getLongKey() + "", e.getValue().save(new CompoundTag()));
            }
            tag.put("spawners", spawners);
        }

        if(name != null) tag.putString("name", getName());
        if(world != null) tag.putString("world", getWorld().getName());

        if(bounds != null) tag.put("bounds", getBounds().save());
    }

    @Override
    public void load(CompoundTag tag) {
        if(tag.get("bounds") instanceof IntArrayTag arr) {
            setBounds(Bounds3i.of(arr));
        }

        if(tag.get("world") instanceof StringTag str) {
            setWorld(Bukkit.getWorld(str.getAsString()));
        }

        if(tag.get("name") instanceof StringTag str) {
            setName(str.getAsString());
        }

        this.spawners.clear();

        if(tag.get("spawners") instanceof CompoundTag spawners) {
            for (Map.Entry<String, Tag> e: spawners.tags.entrySet()) {
                long pos = Long.parseLong(e.getKey());
                BlockPos bPos = BlockPos.of(pos);

                BaseSpawner s = createEmpty();

                s.load(getVanillaWorld(), bPos, (CompoundTag) e.getValue());
                this.spawners.put(pos, s);
            }
        }
    }

    @Override
    public void placeSpawners() {
        ServerLevel level = getVanillaWorld();

        for (Long2ObjectMap.Entry<BaseSpawner> e: getSpawners().long2ObjectEntrySet()) {
            BlockPos pos = BlockPos.of(e.getLongKey());
            place(e.getValue(), level, pos);
        }
    }

    @Override
    public LongList getExistingSpawners() {
        ServerLevel level = getVanillaWorld();
        LongList result = new LongArrayList();

        for (long l: getSpawnerPositions()) {
            BlockState state = level.getBlockState(BlockPos.of(l));

            if (!state.is(Blocks.AIR)
                    && !state.is(Blocks.VOID_AIR)
                    && !state.is(Blocks.CAVE_AIR)
            ) {
                result.add(l);
            }
        }

        return result;
    }

    @Override
    public void addSpawner(Block bBlock) {
        CraftBlock block = (CraftBlock) bBlock;

        Validate.isTrue(block.getType() == Material.SPAWNER, "Block is not spawner");

        ServerLevel level = getVanillaWorld();
        SpawnerBlockEntity entity = (SpawnerBlockEntity) level.getBlockEntity(block.getPosition());

        addSpawner(Vector3i.of(block.getPosition()), entity.getSpawner().save(new CompoundTag()));
    }

    @Override
    public void addSpawner(Vector3i pos, CompoundTag tag) {
        BaseSpawner s = createEmpty();
        s.load(getVanillaWorld(), pos.toNms(), tag);

        spawners.put(pos.toLong(), s);
    }

    @Override
    public boolean isClear() {
        return getExistingSpawners().isEmpty();
    }

    @Override
    public SpawnerView view(Player player) {
        ServerPlayer sPlayer = VanillaAccess.getPlayer(player);
        LongList existing = getExistingSpawners();
        if(existing.isEmpty()) return null;

        SpawnerView prev = getViewers().get(player);
        if(prev != null) {
            stopViewing(prev);
        }

        Slime[] slimes = new Slime[existing.size()];

        for (int i = 0; i < slimes.length; i++) {
            long pos = existing.getLong(i);
            BlockPos bPos = BlockPos.of(pos);
            Vec3 vecPos = new Vec3(bPos.getX() + 0.5D, bPos.getY(), bPos.getZ() + 0.5D);

            Slime s = new Slime(EntityType.SLIME, getVanillaWorld());
            s.setSize(2, true);
            s.setInvisible(true);
            s.setGlowingTag(true);
            s.setInvulnerable(true);
            s.setNoGravity(true);
            s.setPos(vecPos);
            s.setCustomNameVisible(true);
            s.setCustomName(new TextComponent(vecPos.x + " " + vecPos.y + " " + vecPos.z));

            slimes[i] = s;
            sPlayer.networkManager.send(s.getAddEntityPacket());
        }

        SpawnerView view = new SpawnerView(player, slimes);
        view.startTask(this);

        viewers.put(player, view);

        return view;
    }

    @Override
    public void stopViewing(SpawnerView view) {
        view.stopTask();

        ServerPlayer player = VanillaAccess.getPlayer(view.getViewer());
        IntList removeIDs = new IntArrayList();

        for (Slime s: view.getSlimes()) {
            removeIDs.add(s.getId());
        }

        player.networkManager.send(new ClientboundRemoveEntitiesPacket(removeIDs));
        getViewers().remove(view.getViewer());

        if(viewers.isEmpty()) {
            viewers = null;
        }
    }

    @Override
    public Bounds3i getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(Bounds3i bounds) {
        this.bounds = bounds;
    }

    @Override
    public LongSet getSpawnerPositions() {
        return spawners.keySet();
    }

    @Override
    public BaseSpawner getSpawner(long pos) {
        return spawners.get(pos);
    }

    public Long2ObjectMap<BaseSpawner> getSpawners() {
        return spawners;
    }

    Map<Player, SpawnerView> getViewers() {
        return viewers == null ? viewers = new Object2ObjectOpenHashMap<>() : viewers;
    }

    ServerLevel getVanillaWorld() {
        return VanillaAccess.getLevel(getWorld());
    }

    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public static BaseSpawner createEmpty() {
        return new BaseSpawner() {
            @Override
            public void broadcastEvent(Level world, BlockPos pos, int i) {
                world.blockEvent(pos, Blocks.SPAWNER, i, 0);
            }
        };
    }

    public static void place(BaseSpawner spawner, ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(),  2 | 16 | 1024); // NOTIFY | NO_OBSERVER | NO_PLACE (custom)
        SpawnerBlockEntity entity = new SpawnerBlockEntity(pos, Blocks.SPAWNER.defaultBlockState());
        entity.load(spawner.save(new CompoundTag()));

        level.setBlockEntity(entity);
    }
}
