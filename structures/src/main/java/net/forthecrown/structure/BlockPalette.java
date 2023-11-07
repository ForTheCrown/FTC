package net.forthecrown.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.TagTypes;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.block.CommandBlock;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class BlockPalette {
  /* ----------------------------- CONSTANTS ------------------------------ */

  public static final String
      TAG_ENTITIES = "entities",
      TAG_POS_LIST = "positions",
      TAG_BLOCKS = "blocks",
      TAG_SIZE = "size";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  private final BlockStructure structure;
  private final List<EntityInfo> entities = new ObjectArrayList<>();
  private final Map<BlockInfo, LongList> block2Positions = new Object2ObjectOpenHashMap<>();

  @Getter
  private Vector3i size = Vector3i.ZERO;

  /* ----------------------------- METHODS ------------------------------ */

  public void clear() {
    size = Vector3i.ZERO;
    entities.clear();
    block2Positions.clear();
  }

  public void fill(StructureFillConfig config) {
    clear();

    var area = config.getArea();
    this.size = area.size();

    Vector3d origin = area.min().toDouble();
    int blocks = 0;

    for (var b : area) {
      if (!config.includeBlock(b)) {
        continue;
      }

      if (b.getState() instanceof CommandBlock cmd
          && config.isIncludingFunctionBlocks()
          && cmd.getCommand().contains(FunctionInfo.FUNCTION_CMD_PREFIX)
      ) {
        try {
          var info = FunctionInfo.parse(origin, cmd);
          structure.functions.add(info);
        } catch (CommandSyntaxException exc) {
          Loggers.getLogger().error("Couldn't parse function block at {}:",
              Vectors.from(b), exc
          );
        }

        continue;
      }

      BlockInfo info = BlockInfo.of(b);
      LongList posList = block2Positions.computeIfAbsent(info, info1 -> new LongArrayList());
      Vector3i offset = Vectors.from(b).sub(origin.toInt());

      posList.add(Vectors.toLong(offset));
      ++blocks;
    }

    area.entities().forEachRemaining(e -> {
      if (!config.includeEntity(e)) {
        return;
      }

      EntityInfo info = EntityInfo.of(origin, e);
      this.entities.add(info);
    });

    Loggers.getLogger().info("Scanned {} blocks and {} entities",
        blocks, entities.size()
    );
  }

  public void place(StructurePlaceConfig config) {
    for (var i : block2Positions.entrySet()) {
      for (var l : i.getValue()) {
        Vector3i offset = Vectors.fromLong(l);
        var info = config.run(i.getKey(), offset);

        if (info == null || info.left() == null) {
          continue;
        }

        info.left().place(config, info.right());
      }
    }

    if (config.getEntitySpawner() != null) {
      for (var e : entities) {
        e.place(config);
      }
    }

    for (var f : structure.functions) {
      var processor = config.getFunctions().get(f.getFunctionKey());

      if (processor != null) {
        processor.process(f, config);
      } else {
        f.place(config);
      }
    }
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public void save(CompoundTag tag) {
    tag.put(TAG_SIZE, Vectors.writeTag(size));

    if (!block2Positions.isEmpty()) {
      var blockTag = BinaryTags.listTag();

      for (var e : block2Positions.entrySet()) {
        CompoundTag bTag = BinaryTags.compoundTag();
        e.getKey().save(bTag);

        bTag.putLongArray(TAG_POS_LIST, e.getValue());
        blockTag.add(bTag);
      }

      tag.put(TAG_BLOCKS, blockTag);
    }

    if (!entities.isEmpty()) {
      ListTag eTag = TagUtil.writeList(entities, EntityInfo::save);
      tag.put(TAG_ENTITIES, eTag);
    }
  }

  public void load(CompoundTag tag, int oldVersion, int newVersion) {
    clear();

    if (tag.containsKey(TAG_BLOCKS)) {
      ListTag list = tag.getList(TAG_BLOCKS, TagTypes.compoundType());

      for (var t : list) {
        CompoundTag bTag = (CompoundTag) t;
        var positions = bTag.getLongArray(TAG_POS_LIST);
        BlockInfo info = BlockInfo.load(bTag);

        info = StructureDataFixer.fix(info, oldVersion, newVersion);

        block2Positions.put(info, new LongArrayList(positions));
      }
    }

    if (tag.containsKey(TAG_ENTITIES)) {
      entities.addAll(TagUtil.readList(
          tag.get(TAG_ENTITIES).asList(),
          tag1 -> {
            var info = EntityInfo.load(tag1);
            return StructureDataFixer.fix(info, oldVersion, newVersion);
          }
      ));
    }

    if (tag.containsKey(TAG_SIZE)) {
      this.size = Vectors.read3i(tag.get(TAG_SIZE));
    } else {
      scanSize();
    }
  }

  private void scanSize() {
    Vector3i min = Vector3i.from(Integer.MAX_VALUE);
    Vector3i max = Vector3i.from(Integer.MIN_VALUE);

    for (var s : block2Positions.values()) {
      for (var l : s) {
        Vector3i pos = Vectors.fromLong(l);
        min = min.min(pos);
        max = max.max(pos);
      }
    }

    this.size = max.sub(min).add(Vector3i.ONE);
  }
}