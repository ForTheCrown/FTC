package net.forthecrown.dungeons.level.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Vectors;
import org.spongepowered.math.vector.Vector3i;

@Getter
public class TreeGeneratorConfig {

  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(Vector3i.class, Vectors.V3I_ADAPTER)
      .create();

  /**
   * Max depth, to avoid infinite growth
   */
  private final int maxDepth;
  private final int minDepth;

  private final int minRoomDepth;
  private final int maxRoomDepth;

  private final int minConnectorDepth;
  private final int maxConnectorDepth;

  private final int maxConnectorExits;
  private final int maxRoomExits;

  /**
   * The amount of 'potential' levels made by the generator, from which the best level is selected
   */
  private final int potentialLevels;

  /**
   * Required amount of non-connector rooms
   */
  private final int requiredRooms;

  /**
   * Chance for a room to have open gates and thus allow for further nodes to be developed from it
   */
  private final float roomOpenChance;

  private final float decorateGateRate;

  /**
   * Tree generation location
   */
  private final Vector3i location;

  /**
   * 0.0 to 1.0 chance a chest location will result in a chest
   */
  private final float chestRate;

  private transient final Random random;
  private final long seed;

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

    this.random = new Random(builder.seed);
    this.seed = builder.seed;

    this.potentialLevels = builder.potentialLevels;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static TreeGeneratorConfig defaultConfig() {
    return builder().build();
  }

  public static TreeGeneratorConfig deserialize(JsonElement element) {
    return GSON.fromJson(element, Builder.class).build();
  }

  public JsonElement serialize() {
    return GSON.toJsonTree(this);
  }

  @Getter
  @Setter
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

    private int potentialLevels = 35;
    private Vector3i location = Vector3i.ZERO;
    private float chestRate = 0.3f;
    private long seed = Util.RANDOM.nextLong();

    public TreeGeneratorConfig build() {
      return new TreeGeneratorConfig(this);
    }
  }
}