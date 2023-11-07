package net.forthecrown.structure.buffer;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Transform;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.spongepowered.math.vector.Vector3i;

@Getter
public class ChunkedBlockBuffer implements BlockBuffer {

  static final int SHIFT_X = 8;
  static final int SHIFT_Y = 4;
  static final int SHIFT_Z = 0;
  static final int BIT_MASK = 0xF;

  static final int SECTION_BITS = 4;
  static final int SECTION_SIZE = 1 << SECTION_BITS;

  static final int SECTION_SIZE_CUBED = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;

  @Getter
  private final Bounds3i bounds;

  private final Vector3i chunkSize;
  private final int chunkXYSize;

  private final int offX;
  private final int offY;
  private final int offZ;

  final Section[] sections;

  public ChunkedBlockBuffer(Bounds3i bounds) {
    Objects.requireNonNull(bounds);
    this.bounds = bounds;

    Bounds3i expanded = bounds.expand(SECTION_SIZE);
    Vector3i min = expanded.min();
    Vector3i max = expanded.max();

    Vector3i cMin = toSection(min);
    Vector3i cMax = toSection(max);
    this.chunkSize = cMax.sub(cMin);
    this.chunkXYSize = chunkSize.x() * chunkSize.y();

    // Calculate offset of the buffer relative to the
    // world's origin point
    this.offX = cMin.x() << SECTION_BITS;
    this.offY = cMin.y() << SECTION_BITS;
    this.offZ = cMin.z() << SECTION_BITS;

    // Create section array
    int arrSize = chunkSize.x() * chunkSize.y() * chunkSize.z();
    this.sections = new Section[arrSize];
  }

  Vector3i toSection(Vector3i p) {
    return Vector3i.from(
        p.x() >> SECTION_BITS,
        p.y() >> SECTION_BITS,
        p.z() >> SECTION_BITS
    );
  }

  Vector3i fromSection(Vector3i p) {
    return Vector3i.from(
        p.x() << SECTION_BITS,
        p.y() << SECTION_BITS,
        p.z() << SECTION_BITS
    );
  }


  // Following 2 methods I got from here:
  // https://ennogames.com/blog/3d-and-2d-coordinates-to-1d-indexes

  public int toSectionIndex(int x, int y, int z) {
    // First the coodinates must be offset by the buffer's boundaries,
    // then bit shifted, to convert them to section coordinates,
    // then they are calculated into a 1D index for the section's array
    return ((x - offX) >> SECTION_BITS)
         + ((y - offY) >> SECTION_BITS) * chunkSize.x()
         + ((z - offZ) >> SECTION_BITS) * chunkXYSize;
  }

  public Vector3i fromSectionIndex(int index) {
    return Vector3i.from(
        index % chunkSize.x(),
        (index / chunkSize.x()) % chunkSize.y(),
        index / chunkXYSize
    );
  }

  @Override
  public CompletableFuture<Void> place(
      World world,
      Transform transform,
      boolean updatePhysics
  ) {
    return new ChunkedBufferPlacement(world, this, transform, updatePhysics).start();
  }

  @Override
  public BufferBlock getBlock(int x, int y, int z) {
    int index = toSectionIndex(x, y, z);

    if (index < 0 || index >= sections.length) {
      return null;
    }

    var section = sections[index];

    if (section == null) {
      return null;
    }

    return section.getBlock(x, y, z);
  }

  @Override
  public void setBlock(int x, int y, int z, BufferBlock block) {
    int index = toSectionIndex(x, y, z);

    if (index < 0 || index >= sections.length) {
      return;
    }

    Section section = sections[index];

    if (section == null) {
      Vector3i originChunk = fromSectionIndex(index);
      section = new Section(
          fromSection(originChunk).add(offX, offY, offZ)
      );

      sections[index] = section;
    }

    section.setBlock(x, y, z, block);
  }

  @Getter
  @RequiredArgsConstructor
  static class Section {
    final Vector3i origin;

    final BufferBlock[] blocks = new BufferBlock[SECTION_SIZE_CUBED];

    int toIndex(int sx, int sy, int sz) {
      return ((sx & BIT_MASK) << SHIFT_X)
           | ((sy & BIT_MASK) << SHIFT_Y)
           | ((sz & BIT_MASK) << SHIFT_Z);
    }

    Vector3i fromIndex(int index) {
      int x = (index >> SHIFT_X) & BIT_MASK;
      int y = (index >> SHIFT_Y) & BIT_MASK;
      int z = (index >> SHIFT_Z) & BIT_MASK;

      return Vector3i.from(x, y, z);
    }

    void place(World world, Transform transform, boolean update) {
      for (int i = 0; i < blocks.length; i++) {
        BufferBlock block = blocks[i];

        if (block == null) {
          continue;
        }

        Vector3i pos = transform.apply(origin.add(fromIndex(i)));
        Block bukkit = Vectors.getBlock(pos, world);
        block.applyTo(bukkit, update);
      }
    }

    BufferBlock getBlock(int rx, int ry, int rz) {
      return blocks[toIndex(rx, ry, rz)];
    }

    void setBlock(int rx, int ry, int rz, BufferBlock block) {
      blocks[toIndex(rx, ry, rz)] = block;
    }
  }
}