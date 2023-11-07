package net.forthecrown.utils.math;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.DoubleTag;
import net.forthecrown.nbt.FloatTag;
import net.forthecrown.nbt.IntArrayTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.LongArrayTag;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.TrigMath;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2f;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector2l;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3f;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.math.vector.Vector3l;
import org.spongepowered.math.vector.Vector4d;
import org.spongepowered.math.vector.Vector4f;
import org.spongepowered.math.vector.Vector4i;
import org.spongepowered.math.vector.Vector4l;
import org.spongepowered.math.vector.VectorNd;
import org.spongepowered.math.vector.VectorNf;
import org.spongepowered.math.vector.VectorNi;
import org.spongepowered.math.vector.VectorNl;
import org.spongepowered.math.vector.Vectord;
import org.spongepowered.math.vector.Vectorf;
import org.spongepowered.math.vector.Vectori;
import org.spongepowered.math.vector.Vectorl;

/**
 * This class was created using a generator, if one method has a bug or error, they all have it
 * lmao
 */
public final class Vectors {

  /**
   * Name of the X axis: 'x'
   */
  public static final String
  AXIS_X = "x",

  /**
   * Name of the Y axis: 'y'
   */
  AXIS_Y = "y",

  /**
   * Name of the Z Axis: 'z'
   */
  AXIS_Z = "z",

  /**
   * Name of the W axis: 'w'
   */
  AXIS_W = "w";

  /**
   * ID/index of the X axis
   */
  public static final int
      AXIS_ID_X = 0,

  /**
   * ID/index of the Y Axis
   */
  AXIS_ID_Y = 1,

  /**
   * ID/index of the Z Axis
   */
  AXIS_ID_Z = 2,

  /**
   * ID/index of the W Axis
   */
  AXIS_ID_W = 3;

  /**
   * Axis ID to Axis name array
   */
  public static final String[] AXES = {
      AXIS_X,
      AXIS_Y,
      AXIS_Z,
      AXIS_W,
  };

  /*
   * IDs represent the position of
   * an axis within the toArray() result
   * of vectors, they are basically just
   * indexes
   */

  public static final int NAMEABLE_AXES_LENGTH = AXES.length;

  /**
   * The amount of bits to shift a coordinate by to convert to or from a chunk coordinate
   */
  public static final int CHUNK_BITS = 4;

  /**
   * The size of a chunk in blocks
   */
  public static final int CHUNK_SIZE = 1 << CHUNK_BITS;

  public static final long CHUNK_COORDINATE_MASK = 0xFFFFFFFFL;

  public static final long CHUNK_COORDINATE_SIZE = 32;

  public static final Codec<Vector3i> V3I_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.INT.fieldOf("x").forGetter(Vector3i::x),
            Codec.INT.fieldOf("y").forGetter(Vector3i::y),
            Codec.INT.fieldOf("z").forGetter(Vector3i::z)
        )
        .apply(instance, Vector3i::from);
  });

  public static final Codec<Vector3d> V3D_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vector3d::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vector3d::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vector3d::z)
        )
        .apply(instance, Vector3d::from);
  });

  public static final Codec<Vector3f> V3F_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.FLOAT.fieldOf("x").forGetter(Vector3f::x),
            Codec.FLOAT.fieldOf("y").forGetter(Vector3f::y),
            Codec.FLOAT.fieldOf("z").forGetter(Vector3f::z)
        )
        .apply(instance, Vector3f::from);
  });

  /* --------------------------- TYPE ADAPTERS ---------------------------- */

  public static final TypeAdapter<Vector3i> V3I_ADAPTER
      = JsonUtils.createAdapter(Vectors::writeJson, Vectors::read3i);

  public static final TypeAdapter<Vector3d> V3D_ADAPTER
      = JsonUtils.createAdapter(Vectors::writeJson, Vectors::read3d);

  private Vectors() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /* ----------------------------------------------------------- */

  /**
   * @param radius       the radius of the circle
   * @param amountPoints the amount of points that make up the circle
   * @param extraY       Extra y to add to the locs
   * @return a list of locations that make up a circle
   */
  public static List<Vector3d> getCirclePoints(double extraY, double radius, int amountPoints) {
    List<Vector3d> result = new ArrayList<>();
    for (int i = 0; i < amountPoints; ++i) {
      final double angle = Math.toRadians(((double) i / amountPoints) * 360d);

      double x = Math.cos(angle) * radius;
      double z = Math.sin(angle) * radius;

      result.add(new Vector3d(x, extraY, z));
    }
    return result;
  }

  /* ---------------------------------------------------------------------- */

  public static double getYaw(Vector3d v) {
    double x = v.x();
    double z = v.z();
    double atan2 = TrigMath.atan2(-x, z);
    return Math.toDegrees((atan2 + TrigMath.TWO_PI) % TrigMath.TWO_PI);
  }

  public static double getPitch(Vector3d v) {
    double x = v.x();
    double y = v.y();
    double z = v.z();

    if (x == 0.0D && z == 0.0D) {
      return y > 0.0D ? -90.0D : 90.0D;
    } else {
      return Math.toDegrees(TrigMath.atan(-y / GenericMath.sqrt(x * x + z * z)));
    }
  }

  /* ------------------------ BUKKIT CONVERSTIONS ------------------------- */

  public static Vector3i from(Block block) {
    return Vector3i.from(block.getX(), block.getY(), block.getZ());
  }

  public static Block getBlock(Vector3i v, World w) {
    return w.getBlockAt(v.x(), v.y(), v.z());
  }

  public static Vector toVec(Vector3i v) {
    return new Vector(v.x(), v.y(), v.z());
  }

  public static Vector toVec(Vector3d v) {
    return new Vector(v.x(), v.y(), v.z());
  }

  public static Vector3i intFrom(Location l) {
    return Vector3i.from(l.getBlockX(), l.getBlockY(), l.getBlockZ());
  }

  public static Vector3i intFrom(Vector l) {
    return Vector3i.from(l.getBlockX(), l.getBlockY(), l.getBlockZ());
  }

  public static Vector3d doubleFrom(Location l) {
    return Vector3d.from(l.getX(), l.getY(), l.getZ());
  }

  public static Vector3d doubleFrom(Vector l) {
    return Vector3d.from(l.getX(), l.getY(), l.getZ());
  }

  public static Vector3i from(BlockFace face) {
    return Vector3i.from(face.getModX(), face.getModY(), face.getModZ());
  }

  /* ----------------------------- MINECRAFT CONVERSIONS ------------------------------ */

  public static BlockPos toMinecraft(Vector3i v) {
    return new BlockPos(v.x(), v.y(), v.z());
  }

  public static Vec3 toMinecraft(Vector3d v) {
    return new Vec3(v.x(), v.y(), v.z());
  }

  public static Vector2i getChunk(Vector3i v) {
    return new Vector2i(
        v.x() >> CHUNK_BITS,
        v.z() >> CHUNK_BITS
    );
  }

  public static long toChunkLong(Vector2i pos) {
    return toChunkLong(pos.x(), pos.y());
  }

  public static long toChunkLong(int chunkX, int chunkZ) {
    return (long) chunkX & CHUNK_COORDINATE_MASK
        | ((long) chunkZ & CHUNK_COORDINATE_MASK) << CHUNK_COORDINATE_SIZE;
  }

  public static Vector2i fromChunkLong(long l) {
    int x = (int) (l & CHUNK_COORDINATE_MASK);
    int z = (int) (l >>> CHUNK_COORDINATE_SIZE & CHUNK_COORDINATE_MASK);
    return Vector2i.from(x, z);
  }

  public static long toLong(Vector3i v) {
    // Copied from net.minecraft.core.BlockPos.asLong(int, int, int)
    // ^ Short way of saying I do not understand this at all lol
    // All I know is it packs coordinates, so it uses less space
    // and works for what I need
    return (((long) v.x() & 0x3FFFFFF) << 38)
        | (((long) v.y() & 0xFFF))
        | (((long) v.z() & 0x3FFFFFF) << 12);
  }

  public static Vector3i fromLong(long l) {
    var bPos = BlockPos.of(l);
    return Vector3i.from(bPos.getX(), bPos.getY(), bPos.getZ());
  }

  public static int toChunk(int block) {
    return block >> CHUNK_BITS;
  }

  public static int toBlock(int chunk) {
    return chunk << CHUNK_BITS;
  }

  /* ----------------------------- INT VECTORS ------------------------------ */

  public static JsonElement writeJson(Vectori vec) {
    int[] arr = vec.toArray();

    if (arr.length > NAMEABLE_AXES_LENGTH) {
      JsonArray result = new JsonArray();

      for (int val : arr) {
        result.add(val);
      }

      return result;
    }
    JsonWrapper json = JsonWrapper.create();

    for (int i = 0; i < arr.length; i++) {
      json.add(AXES[i], arr[i]);
    }

    return json.getSource();
  }

  public static BinaryTag writeTag(Vectori vec) {
    int[] arr = vec.toArray();
    return BinaryTags.intArrayTag(arr);
  }

  public static Vector2i read2i(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector2i.from(
        json.getInt(AXIS_X),
        json.getInt(AXIS_Y)
    );
  }

  public static Vector2i read2i(BinaryTag tag) {
    int[] arr = ((IntArrayTag) tag).toIntArray();
    return Vector2i.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y]
    );
  }

  public static Vector3i read3i(JsonElement element) {
    if (element.isJsonArray()) {
      var arr = element.getAsJsonArray();

      return Vector3i.from(
          arr.get(AXIS_ID_X).getAsInt(),
          arr.get(AXIS_ID_Y).getAsInt(),
          arr.get(AXIS_ID_Z).getAsInt()
      );
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector3i.from(
        json.getInt(AXIS_X),
        json.getInt(AXIS_Y),
        json.getInt(AXIS_Z)
    );
  }

  public static Vector3i read3i(BinaryTag tag) {
    int[] arr = ((IntArrayTag) tag).toIntArray();
    return Vector3i.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y],
        arr[AXIS_ID_Z]
    );
  }

  public static Vector4i read4i(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector4i.from(
        json.getInt(AXIS_X),
        json.getInt(AXIS_Y),
        json.getInt(AXIS_Z),
        json.getInt(AXIS_W)
    );
  }

  public static Vector4i read4i(BinaryTag tag) {
    int[] arr = ((IntArrayTag) tag).toIntArray();
    return Vector4i.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y],
        arr[AXIS_ID_Z],
        arr[AXIS_ID_W]
    );
  }

  public static VectorNi readNi(JsonElement element) {
    JsonArray arr = element.getAsJsonArray();
    int[] resultArr = new int[arr.size()];

    for (int i = 0; i < arr.size(); i++) {
      int val = arr.get(i).getAsInt();
      resultArr[i] = val;
    }

    return new VectorNi(resultArr);
  }

  public static VectorNi readNi(BinaryTag tag) {
    int[] arr = ((IntArrayTag) tag).toIntArray();
    return new VectorNi(arr);
  }

  // --- LONG VECTORS --- //

  public static JsonElement writeJson(Vectorl vec) {
    long[] arr = vec.toArray();

    if (arr.length > NAMEABLE_AXES_LENGTH) {
      JsonArray result = new JsonArray();

      for (long val : arr) {
        result.add(val);
      }

      return result;
    }
    JsonWrapper json = JsonWrapper.create();

    for (int i = 0; i < arr.length; i++) {
      json.add(AXES[i], arr[i]);
    }

    return json.getSource();
  }

  public static BinaryTag writeTag(Vectorl vec) {
    long[] arr = vec.toArray();
    return BinaryTags.longArrayTag(arr);
  }

  public static Vector2l read2l(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector2l.from(
        json.getLong(AXIS_X),
        json.getLong(AXIS_Y)
    );
  }

  public static Vector2l read2l(BinaryTag tag) {
    long[] arr = ((LongArrayTag) tag).toLongArray();
    return Vector2l.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y]
    );
  }

  public static Vector3l read3l(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector3l.from(
        json.getLong(AXIS_X),
        json.getLong(AXIS_Y),
        json.getLong(AXIS_Z)
    );
  }

  public static Vector3l read3l(BinaryTag tag) {
    long[] arr = ((LongArrayTag) tag).toLongArray();
    return Vector3l.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y],
        arr[AXIS_ID_Z]
    );
  }

  public static Vector4l read4l(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector4l.from(
        json.getLong(AXIS_X),
        json.getLong(AXIS_Y),
        json.getLong(AXIS_Z),
        json.getLong(AXIS_W)
    );
  }

  public static Vector4l read4l(BinaryTag tag) {
    long[] arr = ((LongArrayTag) tag).toLongArray();
    return Vector4l.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y],
        arr[AXIS_ID_Z],
        arr[AXIS_ID_W]
    );
  }

  public static VectorNl readNl(JsonElement element) {
    JsonArray arr = element.getAsJsonArray();
    long[] resultArr = new long[arr.size()];

    for (int i = 0; i < arr.size(); i++) {
      long val = arr.get(i).getAsLong();
      resultArr[i] = val;
    }

    return new VectorNl(resultArr);
  }

  public static VectorNl readNl(BinaryTag tag) {
    long[] arr = ((LongArrayTag) tag).toLongArray();
    return new VectorNl(arr);
  }

  // --- FLOAT VECTORS --- //

  public static JsonElement writeJson(Vectorf vec) {
    float[] arr = vec.toArray();

    if (arr.length > NAMEABLE_AXES_LENGTH) {
      JsonArray result = new JsonArray();

      for (float val : arr) {
        result.add(val);
      }

      return result;
    }
    JsonWrapper json = JsonWrapper.create();

    for (int i = 0; i < arr.length; i++) {
      json.add(AXES[i], arr[i]);
    }

    return json.getSource();
  }

  public static BinaryTag writeTag(Vectorf vec) {
    return BinaryTags.floatList(vec.toArray());
  }

  public static Vector2f read2f(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector2f.from(
        json.getFloat(AXIS_X),
        json.getFloat(AXIS_Y)
    );
  }

  public static Vector2f read2f(BinaryTag tag) {
    ListTag listTag = (ListTag) tag;
    float[] arr = new float[listTag.size()];

    for (int i = 0; i < arr.length; i++) {
      arr[i] = ((FloatTag) listTag.get(i)).floatValue();
    }
    return Vector2f.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y]
    );
  }

  public static Vector3f read3f(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector3f.from(
        json.getFloat(AXIS_X),
        json.getFloat(AXIS_Y),
        json.getFloat(AXIS_Z)
    );
  }

  public static Vector3f read3f(BinaryTag tag) {
    ListTag listTag = (ListTag) tag;
    float[] arr = new float[listTag.size()];

    for (int i = 0; i < arr.length; i++) {
      arr[i] = ((FloatTag) listTag.get(i)).floatValue();
    }
    return Vector3f.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y],
        arr[AXIS_ID_Z]
    );
  }

  public static Vector4f read4f(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector4f.from(
        json.getFloat(AXIS_X),
        json.getFloat(AXIS_Y),
        json.getFloat(AXIS_Z),
        json.getFloat(AXIS_W)
    );
  }

  public static Vector4f read4f(BinaryTag tag) {
    ListTag listTag = (ListTag) tag;
    float[] arr = new float[listTag.size()];

    for (int i = 0; i < arr.length; i++) {
      arr[i] = ((FloatTag) listTag.get(i)).floatValue();
    }
    return Vector4f.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y],
        arr[AXIS_ID_Z],
        arr[AXIS_ID_W]
    );
  }

  public static VectorNf readNf(JsonElement element) {
    JsonArray arr = element.getAsJsonArray();
    float[] resultArr = new float[arr.size()];

    for (int i = 0; i < arr.size(); i++) {
      float val = arr.get(i).getAsFloat();
      resultArr[i] = val;
    }

    return new VectorNf(resultArr);
  }

  public static VectorNf readNf(BinaryTag tag) {
    ListTag listTag = (ListTag) tag;
    float[] arr = new float[listTag.size()];

    for (int i = 0; i < arr.length; i++) {
      arr[i] = ((FloatTag) listTag.get(i)).floatValue();
    }
    return new VectorNf(arr);
  }

  // --- DOUBLE VECTORS --- //

  public static JsonElement writeJson(Vectord vec) {
    double[] arr = vec.toArray();

    if (arr.length > NAMEABLE_AXES_LENGTH) {
      JsonArray result = new JsonArray();

      for (double val : arr) {
        result.add(val);
      }

      return result;
    }
    JsonWrapper json = JsonWrapper.create();

    for (int i = 0; i < arr.length; i++) {
      json.add(AXES[i], arr[i]);
    }

    return json.getSource();
  }

  public static BinaryTag writeTag(Vectord vec) {
    return BinaryTags.doubleList(vec.toArray());
  }

  public static Vector2d read2d(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector2d.from(
        json.getDouble(AXIS_X),
        json.getDouble(AXIS_Y)
    );
  }

  public static Vector2d read2d(BinaryTag tag) {
    ListTag listTag = (ListTag) tag;
    double[] arr = new double[listTag.size()];

    for (int i = 0; i < arr.length; i++) {
      arr[i] = ((DoubleTag) listTag.get(i)).doubleValue();
    }
    return Vector2d.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y]
    );
  }

  public static Vector3d read3d(JsonElement element) {
    if (element.isJsonArray()) {
      var arr = element.getAsJsonArray();

      return Vector3d.from(
          arr.get(AXIS_ID_X).getAsDouble(),
          arr.get(AXIS_ID_Y).getAsDouble(),
          arr.get(AXIS_ID_Z).getAsDouble()
      );
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector3d.from(
        json.getDouble(AXIS_X),
        json.getDouble(AXIS_Y),
        json.getDouble(AXIS_Z)
    );
  }

  public static Vector3d read3d(BinaryTag tag) {
    ListTag listTag = (ListTag) tag;
    double[] arr = new double[listTag.size()];

    for (int i = 0; i < arr.length; i++) {
      arr[i] = ((DoubleTag) listTag.get(i)).doubleValue();
    }
    return Vector3d.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y],
        arr[AXIS_ID_Z]
    );
  }

  public static Vector4d read4d(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return Vector4d.from(
        json.getDouble(AXIS_X),
        json.getDouble(AXIS_Y),
        json.getDouble(AXIS_Z),
        json.getDouble(AXIS_W)
    );
  }

  public static Vector4d read4d(BinaryTag tag) {
    ListTag listTag = (ListTag) tag;
    double[] arr = new double[listTag.size()];

    for (int i = 0; i < arr.length; i++) {
      arr[i] = ((DoubleTag) listTag.get(i)).doubleValue();
    }
    return Vector4d.from(
        arr[AXIS_ID_X],
        arr[AXIS_ID_Y],
        arr[AXIS_ID_Z],
        arr[AXIS_ID_W]
    );
  }

  public static VectorNd readNd(JsonElement element) {
    JsonArray arr = element.getAsJsonArray();
    double[] resultArr = new double[arr.size()];

    for (int i = 0; i < arr.size(); i++) {
      double val = arr.get(i).getAsDouble();
      resultArr[i] = val;
    }

    return new VectorNd(resultArr);
  }

  public static VectorNd readNd(BinaryTag tag) {
    ListTag listTag = (ListTag) tag;
    double[] arr = new double[listTag.size()];

    for (int i = 0; i < arr.length; i++) {
      arr[i] = ((DoubleTag) listTag.get(i)).doubleValue();
    }
    return new VectorNd(arr);
  }
}