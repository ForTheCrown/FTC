package net.forthecrown.dungeons.rewrite_4.type;

import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.rewrite_4.DungeonBoss;
import net.forthecrown.dungeons.rewrite_4.component.BossHealth;
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
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public String getName() {
        return name;
    }
}