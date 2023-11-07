package net.forthecrown.structure;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.io.TagOps;

public class StructureDataFixer {

  private static final TypeReference BLOCK_ENTITY = () -> "block_entity";
  private static final TypeReference ENTITY = () -> "entity";

  public static BlockInfo fix(BlockInfo info, int oldVersion, int version) {
    if (!info.hasTag() || oldVersion >= version) {
      return info;
    }

    DataFixer fixer = VanillaAccess.getFixer();
    CompoundTag tag = info.getTag();

    var type = VanillaAccess.getBlockEntityType(info.getData());
    if (type != null) {
      tag.putString("id", type.asString());
    } else {
      return info;
    }

    tag.putInt("x", 1);
    tag.putInt("y", 1);
    tag.putInt("z", 1);

    Dynamic<BinaryTag> dynamic = new Dynamic<>(TagOps.OPS, tag);
    Dynamic<BinaryTag> updated = fixer.update(BLOCK_ENTITY, dynamic, oldVersion, version);

    CompoundTag fixed = updated.getValue().asCompound();
    fixed.remove("x");
    fixed.remove("y");
    fixed.remove("z");

    return info.withTag(fixed);
  }

  public static EntityInfo fix(EntityInfo info, int oldVersion, int version) {
    if (oldVersion >= version) {
      return info;
    }

    CompoundTag tag = info.getTag();
    tag.putString("id", info.getType().toString());

    DataFixer fixer = VanillaAccess.getFixer();
    Dynamic<BinaryTag> dynamic = new Dynamic<>(TagOps.OPS, tag);
    Dynamic<BinaryTag> updated = fixer.update(ENTITY, dynamic, oldVersion, version);

    CompoundTag after = updated.getValue().asCompound();
    after.remove("id");

    return info.withTag(after);
  }
}
