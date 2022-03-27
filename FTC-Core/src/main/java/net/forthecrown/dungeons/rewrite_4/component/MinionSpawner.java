package net.forthecrown.dungeons.rewrite_4.component;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.rewrite_4.BossComponent;
import net.forthecrown.events.Events;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class MinionSpawner extends BossComponent implements TickComponent, BossStatusListener, Listener {
    private static final Random RANDOM = new Random();

    private SpawnPosProvider posProvider;
    private Spawner spawner;
    private int spawnInterval, maxSpawned;

    private final List<Entity> spawned = new ObjectArrayList<>();
    private int tick;

    @Override
    public void tick(long bossTick) {
        tick++;

        if(tick <= spawnInterval) return;
        tick = 0;

        if(spawned.size() >= maxSpawned) return;

        SpawnPosition pos = posProvider.getSpawn(RANDOM);
        Entity e = spawner.spawn(getCurrentContext(), getWorld(), pos.toLocation(getWorld(), RANDOM), RANDOM);
        if(e == null) return;

        spawned.add(e);
    }

    @Override
    public void onBossDeath(BossContext context, boolean forced) {
        Events.unregister(this);
    }

    @Override
    public void onBossSummon(BossContext context) {
        Events.register(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!spawned.contains(event.getEntity())) return;
        spawned.remove(event.getEntity());
    }

    @Override
    public void save(CompoundTag bossTag) {
        CompoundTag tag = new CompoundTag();

        tag.putInt("spawn_interval", spawnInterval);
        tag.putInt("max_spawned", maxSpawned);

        if(spawned instanceof ComplexSpawner complex) {
            tag.put("spawner", complex.save());
        }

        if(posProvider instanceof SpawnPosList list) {
            tag.put("spawns", list.save());
        }

        bossTag.put("minion_spawner", tag);
    }

    @Override
    public void load(CompoundTag bossTag) {
        CompoundTag tag = bossTag.getCompound("minion_spawner");

        spawnInterval = tag.getInt("spawn_interval");
        maxSpawned = tag.getInt("max_spawned");

        Tag t = tag.get("spawner");
        if(t != null) spawner = ComplexSpawner.read(t);

        t = tag.get("spawns");
        if(t != null) {
            posProvider = SpawnPosList.read(t);
        }
    }

    public SpawnPosProvider getPosProvider() {
        return posProvider;
    }

    public void setPosProvider(SpawnPosProvider posProvider) {
        this.posProvider = posProvider;
    }

    public Spawner getSpawner() {
        return spawner;
    }

    public void setSpawner(Spawner spawner) {
        this.spawner = spawner;
    }

    public int getSpawnInterval() {
        return spawnInterval;
    }

    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = spawnInterval;
    }

    public int getMaxSpawned() {
        return maxSpawned;
    }

    public void setMaxSpawned(int maxSpawned) {
        this.maxSpawned = maxSpawned;
    }

    public record SpawnPosition(double x, double y, double z, double randomRange) {
        public static SpawnPosition read(Tag t) {
            ListTag tags = (ListTag) t;

            return new SpawnPosition(
                    tags.getDouble(0),
                    tags.getDouble(1),
                    tags.getDouble(2),
                    tags.getDouble(3)
            );
        }

        public Tag save() {
            ListTag tags = new ListTag();
            tags.add(DoubleTag.valueOf(x));
            tags.add(DoubleTag.valueOf(y));
            tags.add(DoubleTag.valueOf(z));
            tags.add(DoubleTag.valueOf(randomRange));

            return tags;
        }

        public Location toLocation(World w, Random random) {
            double x = applyRand(x(), random);
            double y = y();
            double z = applyRand(z(), random);

            return new Location(w, x, y, z);
        }

        private double applyRand(double val, Random random) {
            return val + (randomRange <= 0 ? 0 : random.nextDouble(randomRange));
        }
    }

    public interface SpawnPosProvider {
        SpawnPosition getSpawn(Random random);
    }

    public static class SpawnPosList extends ObjectArrayList<SpawnPosition> implements SpawnPosProvider {
        public void addRaw(double[][] positions) {
            for (double[] d: positions) {
                SpawnPosition pos = new SpawnPosition(d[0], d[1], d[2], d.length > 3 ? d[4] : 0.0D);
                add(pos);
            }
        }

        @Override
        public SpawnPosition getSpawn(Random random) {
            return get(random.nextInt(size));
        }

        public Tag save() {
            ListTag spawnPosList = new ListTag();

            for (SpawnPosition p: this) {
                spawnPosList.add(p.save());
            }

            return spawnPosList;
        }

        public static SpawnPosList read(Tag t) {
            SpawnPosList list = new SpawnPosList();
            ListTag tags = (ListTag) t;

            for (Tag lTag: tags) {
                list.add(SpawnPosition.read(lTag));
            }

            return list;
        }
    }

    public interface Spawner {
        @Nullable Entity spawn(BossContext context, World w, Location l, Random random);
    }

    public static class ComplexSpawner implements Spawner {
        private final List<NbtSpawnerEntry> entries = new ObjectArrayList<>();

        public void addSpawn(NbtSpawnerEntry entry) {
            entries.add(entry);
        }

        @Nullable
        @Override
        public Entity spawn(BossContext context, World w, Location l, Random random) {
            NbtSpawnerEntry entry = entries.get(random.nextInt(entries.size()));
            return entry.spawn(context, l, w);
        }

        public Tag save() {
            ListTag tags = new ListTag();

            for (NbtSpawnerEntry e: entries) {
                tags.add(e.save());
            }

            return tags;
        }

        public static ComplexSpawner read(Tag t) {
            ListTag tags = (ListTag) t;
            ComplexSpawner spawner = new ComplexSpawner();

            for (Tag tag: tags) {
                spawner.entries.add(NbtSpawnerEntry.read((CompoundTag) tag));
            }

            return spawner;
        }
    }

    public static class NbtSpawnerEntry {
        private boolean dynamicallyAdjust;
        private double baseHealth, baseAttack;
        private EntityType type;
        private CompoundTag data;

        public boolean dynamicallyAdjust() {
            return dynamicallyAdjust;
        }

        public void setDynamicallyAdjust(boolean dynamicallyAdjust) {
            this.dynamicallyAdjust = dynamicallyAdjust;
        }

        public EntityType getType() {
            return type;
        }

        public void setType(EntityType type) {
            this.type = type;
        }

        public CompoundTag getData() {
            return data;
        }

        public void setData(CompoundTag data) {
            this.data = data;
        }

        public double getBaseHealth() {
            return baseHealth;
        }

        public void setBaseHealth(double baseHealth) {
            this.baseHealth = baseHealth;
        }

        public double getBaseAttack() {
            return baseAttack;
        }

        public void setBaseAttack(double baseAttack) {
            this.baseAttack = baseAttack;
        }

        public Entity spawn(BossContext context, Location l, World w) {
            ServerLevel level = VanillaAccess.getLevel(w);
            net.minecraft.world.entity.Entity entity = type.create(level);
            entity.load(data);

            if(entity instanceof net.minecraft.world.entity.LivingEntity living && dynamicallyAdjust) {
                double health = context.health(baseHealth);

                AttributeInstance instance = living.getAttribute(Attributes.MAX_HEALTH);
                instance.removeModifiers();

                instance.setBaseValue(health);
                living.setHealth((float) health);

                AttributeInstance attackInstance = living.getAttribute(Attributes.ATTACK_DAMAGE);
                if(attackInstance != null && baseAttack > 0) {
                    attackInstance.removeModifiers();
                    attackInstance.setBaseValue(context.damage(baseAttack));
                }
            }

            level.addFreshEntity(entity);
            return entity.getBukkitEntity();
        }

        public static NbtSpawnerEntry read(CompoundTag tag) {
            NbtSpawnerEntry entry = new NbtSpawnerEntry();

            entry.dynamicallyAdjust = tag.contains("dynamic");
            entry.baseAttack = tag.getDouble("baseAttack");
            entry.baseHealth = tag.getDouble("baseHealth");

            entry.type = Registry.ENTITY_TYPE.get(ResourceLocation.tryParse(tag.getString("type")));
            entry.data = tag.getCompound("data");

            return entry;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();

            if(dynamicallyAdjust) tag.putBoolean("dynamic", true);
            if(baseAttack > 0.0D) tag.putDouble("baseAttack", baseAttack);
            if(baseHealth > 0.0D) tag.putDouble("baseHealth", baseHealth);

            tag.putString("type", type.id);
            tag.put("data", data);

            return tag;
        }
    }
}