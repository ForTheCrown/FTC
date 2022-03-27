package net.forthecrown.dungeons.rewrite_4.type;

import net.forthecrown.dungeons.boss.SpawnRequirement;
import net.forthecrown.dungeons.rewrite_4.BossIdentifier;
import net.forthecrown.dungeons.rewrite_4.DungeonBoss;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

public abstract class StaticBossType extends BossType {
    protected final DungeonBoss boss;

    public StaticBossType(String name, long id) {
        super(name);

        this.boss = new DungeonBoss(new BossIdentifier(this, id));
        defineBoss(boss);

        boss.setRequirement(createRequirement());
    }

    protected abstract void _defineBoss(DungeonBoss boss);

    protected SpawnRequirement createRequirement() {
        return SpawnRequirement.items(requiredItems());
    }

    protected Collection<ItemStack> requiredItems() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void defineBoss(DungeonBoss boss) {
        super.defineBoss(boss);

        _defineBoss(boss);

    }

    public DungeonBoss getBoss() {
        return boss;
    }

    public BossIdentifier getIdentifier() {
        return getBoss().getIdentifier();
    }
}