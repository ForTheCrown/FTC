package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.NbtSerializable;
import net.forthecrown.utils.JsonUtils;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;

public interface ImmutableBounds3i extends JsonSerializable, NbtSerializable {
    int minX();
    int minY();
    int minZ();

    int maxX();
    int maxY();
    int maxZ();

    default int spanX() {
        return maxX() - minX();
    }

    default int spanY() {
        return maxY() - minY();
    }

    default int spanZ() {
        return maxZ() - minZ();
    }

    default int centerX() {
        return minX() + (spanX() / 2);
    }

    default int centerY() {
        return minY() + (spanY() / 2);
    }

    default int centerZ() {
        return minZ() + (spanZ() / 2);
    }

    default long volume() {
        return (long) spanX() * spanZ() * spanZ();
    }

    default Vector3i center() {
        return new Vector3i(centerX(), centerY(), centerZ());
    }

    default Vector3i span() {
        return new Vector3i(spanX(), spanY(), spanZ());
    }

    default Vector3i min() {
        return new Vector3i(minX(), minY(), minZ());
    }

    default Vector3i max() {
        return new Vector3i(maxX(), maxY(), maxZ());
    }

    boolean contains(int x, int y, int z);
    boolean contains(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    boolean overlaps(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    default int[] toIntArray() {
        return new int[] { minX(), minY(), minZ(), maxX(), maxY(), maxZ() };
    }

    @Override
    default JsonElement serialize() {
        return JsonUtils.writeIntArray(toIntArray());
    }

    @Override
    default Tag save() {
        return new IntArrayTag(toIntArray());
    }
}
