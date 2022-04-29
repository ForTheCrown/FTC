package net.forthecrown.dungeons.rewrite_4;

import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.rewrite_4.component.BossHealth;
import net.forthecrown.dungeons.rewrite_4.component.EmptyRoomCheck;
import net.forthecrown.dungeons.rewrite_4.component.TickController;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public abstract class BossType implements Keyed, Nameable {
    private final String name;
    private final Key key;

    public BossType(String name) {
        this.name = name;
        this.key = Keys.forthecrown(name.toLowerCase().replaceAll(" ", "_"));
    }

    public void defineBoss(DungeonBoss boss) {
        boss.addComponent(new BossHealth());
        boss.addComponent(new TickController());
        boss.addComponent(new EmptyRoomCheck());
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }
}