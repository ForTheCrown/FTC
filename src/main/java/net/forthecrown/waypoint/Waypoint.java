package net.forthecrown.waypoint;

import it.unimi.dsi.fastutil.longs.LongObjectPair;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.core.FTC;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.BoundsHolder;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoint.type.PlayerWaypointType;
import net.forthecrown.waypoint.type.WaypointType;
import net.forthecrown.waypoint.type.WaypointTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static net.forthecrown.user.data.UserTimeTracker.UNSET;

/**
 * A waypoint is a teleport-enabling area where users
 * can teleport to other waypoints which are placed
 * arbitrarily throughout the server.
 */
public class Waypoint implements BoundsHolder {
    private static final Logger LOGGER = FTC.getLogger();

    static final String
            TAG_POS = "position",
            TAG_WORLD = "world",
            TAG_TYPE = "type",
            TAG_PROPERTIES = "properties",
            TAG_RESIDENTS = "residents",
            TAG_LAST_VALID = "lastValid";

    /** The Waypoint's randomly generated UUID */
    @Getter
    private transient final UUID id;

    /**
     * The position of the waypoint.
     * <p>
     * Note: This position will be saved,
     * but the {@link #bounds} field won't,
     * this is because the bounds is created
     * by the waypoint's type
     */
    @Getter
    private Vector3i position = Vector3i.ZERO;

    /**
     * The Bounding box of this waypoint, being
     * inside it means a player can use it.
     * <p>
     * If this waypoint is invulnerable (determined
     * by the {@link WaypointProperties#INVULNERABLE}
     * property) Then any blocks within these bounds
     * will not be destroy-able
     */
    @Getter
    private Bounds3i bounds = Bounds3i.EMPTY;

    /**
     * This Waypoint's world.
     * <p>
     * Reference used here in a similar fashion
     * to {@link org.bukkit.Location}. This is because
     * the underlying world this waypoint is in may
     * be unloaded, deleted or otherwise removed
     */
    private Reference<World> world;

    /**
     * This waypoint's type.
     * <p>
     * If the type is that of a region pole, this
     * waypoint will generate region poles wherever
     * its placed. These poles are also removed when
     * the waypoint is moved or deleted
     */
    @Setter @Getter
    private WaypointType type;

    /** Property values */
    private Object[] properties = ArrayUtils.EMPTY_OBJECT_ARRAY;

    // Right-margin at 80 really do be making me make questionable style
    //   decisions

    /** Map of User UUID to Pair(Inviter UUID, Invite sent timestamp) */
    @Getter
    private final Map<UUID, LongObjectPair<UUID>>
            invites = new Object2ObjectOpenHashMap<>();

    /** Map of Users' UUID to the date they set this waypoint as their home */
    @Getter
    private final Object2LongMap<UUID>
            residents = new Object2LongOpenHashMap<>();

    /**
     * Last time this waypoint was 'valid'
     * <p>
     * Valid means the waypoint's 'pole' exists and
     * all other requirements specified in
     * {@link Waypoints#isValidWaypointArea(Vector3i, PlayerWaypointType, World, boolean)}
     * are filled. This field is used by the day change
     * listener in {@link WaypointManager} to track when
     * this pole was last valid and delete poles that are
     * not valid for extended periods of time
     */
    @Getter @Setter
    private long lastValidTime = UNSET;

    /** Manager this waypoint has been added to */
    WaypointManager manager;

    /* --------------------------- CONSTRUCTORS ---------------------------- */

    public Waypoint() {
        this(UUID.randomUUID());
    }

    public Waypoint(UUID id) {
        this.id = id;
    }

    /* ------------------------------ METHODS ------------------------------- */

    /**
     * Tests if this waypoint has been added to the manager
     * @return True, if this waypoint has been added to
     *         the waypoint manager, false otherwise
     */
    public boolean hasBeenAdded() {
        return manager != null;
    }

    /**
     * Tests if this waypoint's world is loaded
     * @return True, if the world the waypoint is in
     *         is loaded, false otherwise
     */
    public boolean isWorldLoaded() {
        return getWorld() != null;
    }

    /**
     * Sets the position of this waypoint
     * @param position The waypoint's block position
     * @param world The world the waypoint will be moved to
     */
    public void setPosition(@NotNull Vector3i position, @NotNull World world) {
        // Ensure no nulls
        Objects.requireNonNull(position, "Position was null");
        Objects.requireNonNull(world, "World cannot be null");

        // If this is already our location, don't do anything
        if (Objects.equals(position, getPosition())
                && Objects.equals(world, getWorld())
        ) {
            return;
        }

        // If not added, most likely being deserialized, so we don't
        // need to update any actual values
        if (hasBeenAdded() && getWorld() != null) {
            // If type is region_pole, it'll need
            // to destroy the old pole
            type.onPreMove(this, position, world);

            // Remove from the spatial lookup
            manager.getChunkMap()
                    .remove(getWorld(), this);

            // Dynmap may or may not be installed,
            // Here mostly because I don't have dynmap
            // on the test server
            if (DynmapUtil.isInstalled()) {
                WaypointDynmap.updateMarker(this);
            }
        }

        setWorld(world);
        this.position = position;
        this.bounds = type.createBounds().move(position);

        if (hasBeenAdded()) {
            // If the type is region pole, it'll need to
            // place a region pole
            type.onPostMove(this);

            // Add back to spatial lookup, with new
            // position
            manager.getChunkMap()
                    .add(world, this);
        }
    }

    /**
     * Gets the world this waypoint is in
     * @return The waypoint's world, null
     */
    public @Nullable World getWorld() {
        return world == null ? null : world.get();
    }

    private void setWorld(@Nullable World world) {
        if (world == null) {
            this.world = null;
            return;
        }

        this.world = new WeakReference<>(world);
    }

    public String getMarkerId() {
        return "waypoint." + getId();
    }

    /* ---------------------------- PROPERTIES ----------------------------- */

    public <T> T get(WaypointProperty<T> property) {
        if (!has(property)) {
            return property.getDefaultValue();
        }

        return (T) properties[property.getId()];
    }

    public <T> boolean has(WaypointProperty<T> property) {
        int index = property.getId();

        if (index >= properties.length) {
            return false;
        }

        var value = properties[index];

        if (value == null) {
            return false;
        }

        return !value.equals(property.getDefaultValue());
    }

    public <T> boolean set(@NotNull WaypointProperty<T> property, @Nullable T value) {
        T current = get(property);

        // If current value == given value, don't change anything
        if ((current == null && value == null)
                || Objects.equals(current, value)
        ) {
            return false;
        }

        int index = property.getId();

        // If we should unset the value
        if (value == null || Objects.equals(value, property.getDefaultValue())) {
            // If the property value array isn't large enough
            // for the property we want to unset
            if (index >= properties.length) {
                return false;
            }

            value = null;
            properties[index] = null;
        } else {
            properties = ObjectArrays.ensureCapacity(properties, index + 1);
            properties[index] = value;
        }

        // If this method is called before the waypoint is added to
        // the manager, it's most likely because it's being deserialized,
        // during deserialization nothing should be updated as there's
        // no need to update any world data or such
        if (hasBeenAdded()) {
            property.onValueUpdate(this, current, value);
        }

        return true;
    }

    public boolean hasProperties() {
        return ArrayIterator.unmodifiable(properties).hasNext();
    }

    /* ----------------------------- INVITES ------------------------------ */

    public boolean hasValidInvite(UUID uuid) {
        var pair = invites.get(uuid);

        if (pair == null) {
            return false;
        }

        if (Time.isPast(pair.firstLong() + GeneralConfig.validInviteTime)) {
            invites.remove(uuid);
            return false;
        }

        return true;
    }

    public void invite(UUID inviter, UUID target) {
        LongObjectPair<UUID> pair = LongObjectPair.of(
                System.currentTimeMillis(), inviter
        );

        invites.put(target, pair);
    }

    /* ----------------------------- RESIDENTS ------------------------------ */

    public void addResident(UUID uuid) {
        setResident(uuid, System.currentTimeMillis());
    }

    public void setResident(UUID uuid, long time) {
        if (isResident(uuid)) {
            return;
        }

        residents.put(uuid, time);
    }

    public void removeResident(UUID uuid) {
        residents.removeLong(uuid);
    }

    public boolean isResident(UUID uuid) {
        return residents.containsKey(uuid);
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    public void save(CompoundTag tag) {
        WaypointTypes.REGISTRY.writeTag(type)
                        .ifPresent(tag1 -> tag.put(TAG_TYPE, tag1));

        tag.put(TAG_POS, Vectors.writeTag(position));

        var world = getWorld();
        if (world != null) {
            tag.put(TAG_WORLD, TagUtil.writeKey(world.getKey()));
        } else {
            LOGGER.warn("Null world in waypoint {}", getId());
        }

        if (lastValidTime != UNSET) {
            tag.putLong(TAG_LAST_VALID, lastValidTime);
        }

        if (hasProperties()) {
            CompoundTag propTag = new CompoundTag();
            ArrayIterator it = ArrayIterator.unmodifiable(properties);

            while (it.hasNext()) {
                int id = it.nextIndex();
                var next = it.next();

                WaypointProperties.REGISTRY.get(id)
                        .ifPresentOrElse(property -> {
                            if (next == null
                                    || Objects.equals(property.getDefaultValue(), next)
                            ) {
                               return;
                            }

                            Tag pTag = (Tag) property.getSerializer()
                                    .serialize(NbtOps.INSTANCE, next);

                            propTag.put(property.getName(), pTag);
                        }, () -> {
                            LOGGER.warn("Unknown property at index {}", id);
                        });
            }

            tag.put(TAG_PROPERTIES, propTag);
        }

        if (!residents.isEmpty()) {
            CompoundTag rTag = new CompoundTag();
            residents.forEach((uuid, aLong) -> {
                rTag.putLong(uuid.toString(), aLong);
            });

            tag.put(TAG_RESIDENTS, rTag);
        }
    }

    public void load(CompoundTag tag) {
        this.type = WaypointTypes.REGISTRY
                .readTagOrThrow(tag.get(TAG_TYPE));

        this.position = Vectors.read3i(tag.get(TAG_POS));

        if (tag.contains(TAG_WORLD)) {
            var key = TagUtil.readKey(tag.get(TAG_WORLD));

            var world = Bukkit.getWorld(key);
            setWorld(world);
        }

        this.bounds = type.createBounds()
                .move(position);

        if (tag.contains(TAG_LAST_VALID)) {
            lastValidTime = tag.getLong(TAG_LAST_VALID);
        } else {
            lastValidTime = UNSET;
        }

        if (tag.contains(TAG_PROPERTIES)) {
            CompoundTag propertyTag = tag.getCompound(TAG_PROPERTIES);

            for (var e: propertyTag.tags.entrySet()) {
                WaypointProperties.REGISTRY
                        .get(e.getKey())

                        .ifPresentOrElse(property -> {
                            property.getSerializer()
                                    .deserialize(NbtOps.INSTANCE, e.getValue())
                                    .resultOrPartial(LOGGER::warn)
                                    .ifPresent(o -> set(property, o));

                        }, () -> {
                            LOGGER.warn("Unknown property '{}'", e.getKey());
                        });
            }
        } else {
            properties = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }

        if (tag.contains(TAG_RESIDENTS)) {
            var rTag = tag.getCompound(TAG_RESIDENTS);
            rTag.tags.forEach((s, tag1) -> {
                UUID uuid = UUID.fromString(s);
                long time = ((LongTag) tag1).getAsLong();

                setResident(uuid, time);
            });
        } else {
            residents.clear();
        }
    }

    /* ------------------------- OBJECT OVERRIDES -------------------------- */

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Waypoint w)) {
            return false;
        }

        return w.getId().equals(id);
    }
}