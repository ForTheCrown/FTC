package net.forthecrown.structure;

import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractNbtSerializer;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public class FtcStructureManager extends AbstractNbtSerializer {
    public FtcStructureManager() {
        super("structures");

        reload();
        Crown.logger().info("Structures loaded");
    }

    @Override
    protected void save(CompoundTag tag) {
        for (BlockStructure s: Registries.STRUCTURES) {
            tag.put(s.key().asString(), s.save());
        }
    }

    @Override
    protected void reload(CompoundTag tag) {
        Registries.STRUCTURES.clear();

        for (Map.Entry<String, Tag> e: tag.tags.entrySet()) {
            Key key = FtcUtils.parseKey(e.getKey());
            ListTag data = (ListTag) e.getValue();

            BlockStructure structure = new BlockStructure(key);
            structure.load(data);
        }
    }
}
