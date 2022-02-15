package net.forthecrown.dungeons.boss;

import net.forthecrown.core.Keys;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class KeyedBossImpl extends AbstractBoss implements KeyedBoss {
    private final Key key;

    public KeyedBossImpl(String name, FtcBoundingBox room, ItemStack... items) {
        super(name, room, items);

        this.key = Keys.forthecrown(name.toLowerCase().replaceAll(" ", "_"));
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
