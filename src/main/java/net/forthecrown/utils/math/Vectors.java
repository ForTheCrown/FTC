package net.forthecrown.utils.math;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import lombok.experimental.UtilityClass;
import net.forthecrown.utils.io.JsonWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.TrigMath;
import org.spongepowered.math.vector.*;

/**
 * This class was created using a generator,
 * if one method has a bug or error, they all
 * have it lmao
 */
@UtilityClass
public class Vectors {

    /** Name of the X axis: 'x' */
    public final String
            AXIS_X = "x",

            /** Name of the Y axis: 'y' */
            AXIS_Y = "y",

            /** Name of the Z Axis: 'z' */
            AXIS_Z = "z",

            /** Name of the W axis: 'w' */
            AXIS_W = "w";

    /** ID/index of the X axis */
    public final int
            AXIS_ID_X = 0,

            /** ID/index of the Y Axis */
            AXIS_ID_Y = 1,

            /** ID/index of the Z Axis */
            AXIS_ID_Z = 2,

            /** ID/index of the W Axis */
            AXIS_ID_W = 3;

    /** Axis ID to Axis name array */
    public final String[] AXES = {
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

    public final int NAMEABLE_AXES_LENGTH = AXES.length;

    /* ----------------------------- WORLD EDIT CONVERSIONS ------------------------------ */

    public Vector3i from(BlockVector3 v) {
        return Vector3i.from(v.getX(), v.getY(), v.getZ());
    }

    public Vector3d from(Vector3 v) {
        return Vector3d.from(v.getX(), v.getY(), v.getZ());
    }

    public Vector2d from(Vector2 v) {
        return Vector2d.from(v.getX(), v.getZ());
    }

    public Vector2i from(BlockVector2 v) {
        return Vector2i.from(v.getX(), v.getZ());
    }

    public double getYaw(Vector3d v) {
        double x = v.x();
        double z = v.z();
        double atan2 = TrigMath.atan2(-x, z);
        return Math.toDegrees((atan2 + TrigMath.TWO_PI) % + TrigMath.TWO_PI);
    }

    public double getPitch(Vector3d v) {
        double x = v.x();
        double y = v.y();
        double z = v.z();

        if (x == 0.0D && z == 0.0D) {
            return y > 0.0D ? -90.0D : 90.0D;
        } else {
            return Math.toDegrees(TrigMath.atan(-y / GenericMath.sqrt(x * x + z * z)));
        }
    }

    /* ----------------------------- BUKKIT CONVERSTIONS ------------------------------ */

    public Vector3i from(Block block) {
        return Vector3i.from(block.getX(), block.getY(), block.getZ());
    }

    public Block getBlock(Vector3i v, World w) {
        return w.getBlockAt(v.x(), v.y(), v.z());
    }

    public Vector toVec(Vector3i v) {
        return new Vector(v.x(), v.y(), v.z());
    }

    public Vector toVec(Vector3d v) {
        return new Vector(v.x(), v.y(), v.z());
    }

    public Vector3i fromI(Location l) {
        return Vector3i.from(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public Vector3i fromI(Vector l) {
        return Vector3i.from(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public Vector3d fromD(Location l) {
        return Vector3d.from(l.getX(), l.getY(), l.getZ());
    }

    public Vector3d fromD(Vector l) {
        return Vector3d.from(l.getX(), l.getY(), l.getZ());
    }

    public Vector3i from(BlockFace face) {
        return Vector3i.from(face.getModX(), face.getModY(), face.getModZ());
    }

    /* ----------------------------- MINECRAFT CONVERSIONS ------------------------------ */

    public BlockPos toMinecraft(Vector3i v) {
        return new BlockPos(v.x(), v.y(), v.z());
    }

    public Vec3 toMinecraft(Vector3d v) {
        return new Vec3(v.x(), v.y(), v.z());
    }

    public ChunkPos getChunk(Vector3i v) {
        return new ChunkPos(
                v.x() >> 5,
                v.z() >> 5
        );
    }

    public long toLong(Vector3i v) {
        return BlockPos.asLong(v.x(), v.y(), v.z());
    }

    public Vector3i fromLong(long l) {
        var bPos = BlockPos.of(l);
        return Vector3i.from(bPos.getX(), bPos.getY(), bPos.getZ());
    }

    /* ----------------------------- INT VECTORS ------------------------------ */

    public JsonElement writeJson(Vectori vec) {
        int[] arr = vec.toArray();

        if (arr.length > NAMEABLE_AXES_LENGTH) {
            JsonArray result = new JsonArray();

            for (int val: arr) {
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

    public Tag writeTag(Vectori vec) {
        int[] arr = vec.toArray();
        return new IntArrayTag(arr);
    }

    public Vector2i read2i(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector2i.from(
                json.getInt(AXIS_X),
                json.getInt(AXIS_Y)
        );
    }

    public Vector2i read2i(Tag tag) {
        int[] arr = ((IntArrayTag) tag).getAsIntArray();
        return Vector2i.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y]
        );
    }

    public Vector3i read3i(JsonElement element) {
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

    public Vector3i read3i(Tag tag) {
        int[] arr = ((IntArrayTag) tag).getAsIntArray();
        return Vector3i.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y],
                arr[AXIS_ID_Z]
        );
    }

    public Vector4i read4i(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector4i.from(
                json.getInt(AXIS_X),
                json.getInt(AXIS_Y),
                json.getInt(AXIS_Z),
                json.getInt(AXIS_W)
        );
    }

    public Vector4i read4i(Tag tag) {
        int[] arr = ((IntArrayTag) tag).getAsIntArray();
        return Vector4i.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y],
                arr[AXIS_ID_Z],
                arr[AXIS_ID_W]
        );
    }

    public VectorNi readNi(JsonElement element) {
        JsonArray arr = element.getAsJsonArray();
        int[] resultArr = new int[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            int val = arr.get(i).getAsInt();
            resultArr[i] = val;
        }

        return new VectorNi(resultArr);
    }

    public VectorNi readNi(Tag tag) {
        int[] arr = ((IntArrayTag) tag).getAsIntArray();
        return new VectorNi(arr);
    }

    // --- LONG VECTORS --- //

    public JsonElement writeJson(Vectorl vec) {
        long[] arr = vec.toArray();

        if (arr.length > NAMEABLE_AXES_LENGTH) {
            JsonArray result = new JsonArray();

            for (long val: arr) {
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

    public Tag writeTag(Vectorl vec) {
        long[] arr = vec.toArray();
        return new LongArrayTag(arr);
    }

    public Vector2l read2l(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector2l.from(
                json.getLong(AXIS_X),
                json.getLong(AXIS_Y)
        );
    }

    public Vector2l read2l(Tag tag) {
        long[] arr = ((LongArrayTag) tag).getAsLongArray();
        return Vector2l.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y]
        );
    }

    public Vector3l read3l(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector3l.from(
                json.getLong(AXIS_X),
                json.getLong(AXIS_Y),
                json.getLong(AXIS_Z)
        );
    }

    public Vector3l read3l(Tag tag) {
        long[] arr = ((LongArrayTag) tag).getAsLongArray();
        return Vector3l.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y],
                arr[AXIS_ID_Z]
        );
    }

    public Vector4l read4l(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector4l.from(
                json.getLong(AXIS_X),
                json.getLong(AXIS_Y),
                json.getLong(AXIS_Z),
                json.getLong(AXIS_W)
        );
    }

    public Vector4l read4l(Tag tag) {
        long[] arr = ((LongArrayTag) tag).getAsLongArray();
        return Vector4l.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y],
                arr[AXIS_ID_Z],
                arr[AXIS_ID_W]
        );
    }

    public VectorNl readNl(JsonElement element) {
        JsonArray arr = element.getAsJsonArray();
        long[] resultArr = new long[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            long val = arr.get(i).getAsLong();
            resultArr[i] = val;
        }

        return new VectorNl(resultArr);
    }

    public VectorNl readNl(Tag tag) {
        long[] arr = ((LongArrayTag) tag).getAsLongArray();
        return new VectorNl(arr);
    }

    // --- FLOAT VECTORS --- //

    public JsonElement writeJson(Vectorf vec) {
        float[] arr = vec.toArray();

        if (arr.length > NAMEABLE_AXES_LENGTH) {
            JsonArray result = new JsonArray();

            for (float val: arr) {
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

    public Tag writeTag(Vectorf vec) {
        float[] arr = vec.toArray();
        ListTag list = new ListTag();

        for (float val: arr) {
            list.add(FloatTag.valueOf(val));
        }

        return list;
    }

    public Vector2f read2f(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector2f.from(
                json.getFloat(AXIS_X),
                json.getFloat(AXIS_Y)
        );
    }

    public Vector2f read2f(Tag tag) {
        ListTag listTag = (ListTag) tag;
        float[] arr = new float[listTag.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = ((FloatTag) listTag.get(i)).getAsFloat();
        }
        return Vector2f.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y]
        );
    }

    public Vector3f read3f(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector3f.from(
                json.getFloat(AXIS_X),
                json.getFloat(AXIS_Y),
                json.getFloat(AXIS_Z)
        );
    }

    public Vector3f read3f(Tag tag) {
        ListTag listTag = (ListTag) tag;
        float[] arr = new float[listTag.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = ((FloatTag) listTag.get(i)).getAsFloat();
        }
        return Vector3f.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y],
                arr[AXIS_ID_Z]
        );
    }

    public Vector4f read4f(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector4f.from(
                json.getFloat(AXIS_X),
                json.getFloat(AXIS_Y),
                json.getFloat(AXIS_Z),
                json.getFloat(AXIS_W)
        );
    }

    public Vector4f read4f(Tag tag) {
        ListTag listTag = (ListTag) tag;
        float[] arr = new float[listTag.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = ((FloatTag) listTag.get(i)).getAsFloat();
        }
        return Vector4f.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y],
                arr[AXIS_ID_Z],
                arr[AXIS_ID_W]
        );
    }

    public VectorNf readNf(JsonElement element) {
        JsonArray arr = element.getAsJsonArray();
        float[] resultArr = new float[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            float val = arr.get(i).getAsFloat();
            resultArr[i] = val;
        }

        return new VectorNf(resultArr);
    }

    public VectorNf readNf(Tag tag) {
        ListTag listTag = (ListTag) tag;
        float[] arr = new float[listTag.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = ((FloatTag) listTag.get(i)).getAsFloat();
        }
        return new VectorNf(arr);
    }

    // --- DOUBLE VECTORS --- //

    public JsonElement writeJson(Vectord vec) {
        double[] arr = vec.toArray();

        if (arr.length > NAMEABLE_AXES_LENGTH) {
            JsonArray result = new JsonArray();

            for (double val: arr) {
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

    public Tag writeTag(Vectord vec) {
        double[] arr = vec.toArray();
        ListTag list = new ListTag();

        for (double val: arr) {
            list.add(DoubleTag.valueOf(val));
        }

        return list;
    }

    public Vector2d read2d(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector2d.from(
                json.getDouble(AXIS_X),
                json.getDouble(AXIS_Y)
        );
    }

    public Vector2d read2d(Tag tag) {
        ListTag listTag = (ListTag) tag;
        double[] arr = new double[listTag.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = ((DoubleTag) listTag.get(i)).getAsDouble();
        }
        return Vector2d.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y]
        );
    }

    public Vector3d read3d(JsonElement element) {
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

    public Vector3d read3d(Tag tag) {
        ListTag listTag = (ListTag) tag;
        double[] arr = new double[listTag.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = ((DoubleTag) listTag.get(i)).getAsDouble();
        }
        return Vector3d.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y],
                arr[AXIS_ID_Z]
        );
    }

    public Vector4d read4d(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return Vector4d.from(
                json.getDouble(AXIS_X),
                json.getDouble(AXIS_Y),
                json.getDouble(AXIS_Z),
                json.getDouble(AXIS_W)
        );
    }

    public Vector4d read4d(Tag tag) {
        ListTag listTag = (ListTag) tag;
        double[] arr = new double[listTag.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = ((DoubleTag) listTag.get(i)).getAsDouble();
        }
        return Vector4d.from(
                arr[AXIS_ID_X],
                arr[AXIS_ID_Y],
                arr[AXIS_ID_Z],
                arr[AXIS_ID_W]
        );
    }

    public VectorNd readNd(JsonElement element) {
        JsonArray arr = element.getAsJsonArray();
        double[] resultArr = new double[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            double val = arr.get(i).getAsDouble();
            resultArr[i] = val;
        }

        return new VectorNd(resultArr);
    }

    public VectorNd readNd(Tag tag) {
        ListTag listTag = (ListTag) tag;
        double[] arr = new double[listTag.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = ((DoubleTag) listTag.get(i)).getAsDouble();
        }
        return new VectorNd(arr);
    }
}