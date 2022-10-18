package net.forthecrown.dungeons.level;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.registry.RegistryKey;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class PieceTypes {
    private PieceTypes() {}

    public static final Registry<Registry<PieceType>> TYPE_REGISTRY = Registries.newRegistry();

    public static <T extends PieceType> Registry<T> newRegistry(String name) {
        return (Registry<T>) TYPE_REGISTRY.register(name, Registries.newRegistry()).getValue();
    }

    public static PieceType load(Tag t) {
        RegistryKey key = RegistryKey.load(t);

        if (key == null) {
            return null;
        }

        Registry<PieceType> registry = TYPE_REGISTRY.orNull(key.getRegistry());

        if (registry == null) {
            return null;
        }

        return registry.orNull(key.getValue());
    }

    public static StringTag save(PieceType type) {
        for (var r: TYPE_REGISTRY.entries()) {
            var holder = r.getValue().getHolderByValue(type)
                    .map(holder1 -> RegistryKey.of(r.getKey(), holder1.getKey()).save());

            if (holder.isEmpty()) {
                continue;
            }

            return holder.get();
        }

        return null;
    }
}