package net.forthecrown.dungeons.level.generator;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.io.JsonWrapper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Random;

@Getter
public class TreeGeneratorConfig {
    private static final String
            KEY_MAX_DEPTH = "maxDepth",
            KEY_MIN_DEPTH = "minDepth",

            KEY_MIN_ROOM_DEPTH = "minRoomDepth",
            KEY_MAX_ROOM_DEPTH = "maxRoomDepth",

            KEY_MIN_CONNECTOR_DEPTH = "minConnectorDepth",
            KEY_MAX_CONNECTOR_DEPTH = "maxConnectorDepth",

            KEY_MAX_CONNECTOR_EXITS = "maxConnectorExits",
            KEY_MAX_ROOM_EXITS = "maxRoomExits",

            KEY_OPEN_CHANCE = "openRoomChance",
            KEY_REQUIRED_ROOMS = "requiredRoomCount",

            KEY_MAX_GROW_ATTEMPTS = "maxGrowthAttempts",
            KEY_LOCATION = "location",
            KEY_CHEST_CHANCE = "chestChance",
            KEY_SEED = "seed";

    /** Max depth, to avoid infinite growth */
    private final int maxDepth;
    private final int minDepth;

    private final int minRoomDepth;
    private final int maxRoomDepth;

    private final int minConnectorDepth;
    private final int maxConnectorDepth;

    private final int maxConnectorExits;
    private final int maxRoomExits;

    private final int maxGrowthAttempts;

    /** Required amount of non-connector rooms */
    private final int requiredRooms;

    /**
     * Chance for a room to have open gates and thus
     * allow for further nodes to be developed from it
     */
    private final float roomOpenChance;

    private final float decorateGateRate;

    /** Tree generation location */
    private final Vector3i location;

    /** 0.0 to 1.0 chance a chest location will result in a chest */
    private final float chestRate;

    private final Random random;

    public TreeGeneratorConfig(Builder builder) {
        this.maxDepth = builder.maxDepth;
        this.minDepth = builder.minDepth;

        this.minRoomDepth = builder.minRoomDepth;
        this.maxRoomDepth = builder.maxRoomDepth;

        this.minConnectorDepth = builder.minConnectorDepth;
        this.maxConnectorDepth = builder.maxConnectorDepth;

        this.maxConnectorExits = builder.maxConnectorExits;
        this.maxRoomExits = builder.maxRoomExits;

        this.roomOpenChance = builder.roomOpenChance;
        this.requiredRooms = builder.requiredRooms;
        this.decorateGateRate = builder.decorateGateRate;

        this.location = builder.location;
        this.chestRate = builder.chestRate;
        this.random = builder.random;

        this.maxGrowthAttempts = builder.maxGrowthAttempts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TreeGeneratorConfig defaultConfig() {
        return builder().build();
    }

    public static TreeGeneratorConfig deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
        var builder = builder();

        if (json.has(KEY_MAX_DEPTH)) {
            builder.maxDepth(json.getInt(KEY_MAX_DEPTH));
        }

        if (json.has(KEY_MIN_DEPTH)) {
            builder.minDepth(json.getInt(KEY_MIN_DEPTH));
        }

        if (json.has(KEY_MIN_CONNECTOR_DEPTH)) {
            builder.minConnectorDepth(json.getInt(KEY_MIN_CONNECTOR_DEPTH));
        }

        if (json.has(KEY_MAX_CONNECTOR_DEPTH)) {
            builder.maxConnectorDepth(json.getInt(KEY_MAX_CONNECTOR_DEPTH));
        }

        if (json.has(KEY_MAX_CONNECTOR_EXITS)) {
            builder.maxConnectorExits(json.getInt(KEY_MAX_CONNECTOR_EXITS));
        }

        if (json.has(KEY_MAX_ROOM_EXITS)) {
            builder.maxRoomExits(json.getInt(KEY_MAX_ROOM_EXITS));
        }

        if (json.has(KEY_MAX_GROW_ATTEMPTS)) {
            builder.maxGrowthAttempts(json.getInt(KEY_MAX_GROW_ATTEMPTS));
        }

        if (json.has(KEY_LOCATION)) {
            builder.location(Vectors.read3i(json.get(KEY_LOCATION)));
        }

        if (json.has(KEY_CHEST_CHANCE)) {
            builder.chestRate(json.getFloat(KEY_CHEST_CHANCE));
        }

        if (json.has(KEY_SEED)) {
            builder.random(new Random(json.getLong(KEY_SEED)));
        }

        if (json.has(KEY_MIN_ROOM_DEPTH)) {
            builder.minRoomDepth(json.getInt(KEY_MIN_ROOM_DEPTH));
        }

        if (json.has(KEY_MAX_ROOM_DEPTH)) {
            builder.maxRoomDepth(json.getInt(KEY_MAX_ROOM_DEPTH));
        }

        if (json.has(KEY_OPEN_CHANCE)) {
            builder.roomOpenChance(json.getInt(KEY_OPEN_CHANCE));
        }

        if (json.has(KEY_REQUIRED_ROOMS)) {
            builder.requiredRooms(json.getInt(KEY_REQUIRED_ROOMS));
        }

        return builder.build();
    }

    @Getter @Setter
    @Accessors(chain = true, fluent = true)
    public static class Builder {
        private int maxDepth = 10;
        private int minDepth = 5;

        private int minRoomDepth = 1;
        private int maxRoomDepth = 1;

        private int minConnectorDepth = 1;
        private int maxConnectorDepth = 2;

        private int maxConnectorExits = 3;
        private int maxRoomExits = 3;

        private float roomOpenChance = 0.1F;
        private float decorateGateRate = 0.5F;

        private int requiredRooms = 3;

        private int maxGrowthAttempts = 25;
        private Vector3i location = Vector3i.ZERO;
        private float chestRate = 0.3f;
        private Random random = new Random();

        public TreeGeneratorConfig build() {
            return new TreeGeneratorConfig(this);
        }
    }
}