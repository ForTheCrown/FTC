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

    default int sizeX() {
        return (maxX() - minX()) + 1;
    }

    default int sizeY() {
        return (maxY() - minY()) + 1;
    }

    default int sizeZ() {
        return (maxZ() - minZ()) + 1;
    }

    default int centerX() {
        return minX() + (sizeX() / 2);
    }

    default int centerY() {
        return minY() + (sizeY() / 2);
    }

    default int centerZ() {
        return minZ() + (sizeZ() / 2);
    }

    default long volume() {
        return (long) sizeX() * sizeY() * sizeZ();
    }

    default Vector3i center() {
        return new Vector3i(centerX(), centerY(), centerZ());
    }

    default Vector3i size() {
        return new Vector3i(sizeX(), sizeY(), sizeZ());
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

    default Vector3i[] corners() {
        return new Vector3i[] {
                new Vector3i(minX(), minY(), minZ()),
                new Vector3i(minX(), minY(), maxZ()),
                new Vector3i(minX(), maxY(), minZ()),
                new Vector3i(maxX(), minY(), minZ()),
                new Vector3i(minX(), maxY(), maxZ()),
                new Vector3i(maxX(), maxY(), minZ()),
                new Vector3i(maxX(), minY(), maxZ()),
                new Vector3i(maxX(), maxY(), maxZ())
        };
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