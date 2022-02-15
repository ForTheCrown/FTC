package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class DungeonBossGoal implements WeaponKillGoal {
    private final Key key;
    private final int rank, goal;
    private final KeyedBoss boss;

    public DungeonBossGoal(KeyedBoss boss, int goal, int rank) {
        this.rank = rank;
        this.goal = goal;
        this.boss = boss;

        this.key = WeaponGoal.createKey(rank, "dboss_" + boss.getName().toLowerCase().replaceAll(" ", "_"));
    }

    @Override
    public int getGoal() {return goal;}

    @Override
    public int getRank() {return rank;}

    @Override
    public @NotNull Key key() {return key;}

    @Override
    public Component loreDisplay() {return Component.text("Defeat " + boss.getName());}

    @Override
    public boolean test(CrownUser user, Entity entity) {
        // onDeath in DungeonBoss gets called before the weapon listener, getBossEntity() returns null
        if(!entity.getPersistentDataContainer().has(Bosses.BOSS_TAG, PersistentDataType.STRING)) return false;
        Key bossKey = Keys.parse(entity.getPersistentDataContainer().get(Bosses.BOSS_TAG, PersistentDataType.STRING));

        return boss.key().equals(bossKey);
    }
}
