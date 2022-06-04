package net.forthecrown.dungeons.boss;

import net.forthecrown.core.Keys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface DynamicBoss extends DungeonBoss {
    void save(CompoundTag tag);
    void load(CompoundTag tag);

    BossType getType();

    abstract class BossType implements Keyed {
        private final Key key;

        public BossType(String key) {
            this.key = Keys.parse(key);
        }

        public abstract DynamicBoss create();

        @Override
        public @NotNull Key key() {
            return key;
        }
    }
}