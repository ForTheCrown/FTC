package net.forthecrown.inventory.weapon.goals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
@Getter
public class DungeonBossGoal implements WeaponKillGoal {
    private final KeyedBoss boss;
    private final int goal = 1;

    @Override
    public Component loreDisplay() {
        return Component.text("Defeat " + boss.getName());
    }

    @Override
    public boolean test(User user, Entity entity) {
        // onDeath in DungeonBoss gets called before the weapon listener, getBossEntity() returns null
        if (!entity.getPersistentDataContainer()
                .has(Bosses.BOSS_TAG, PersistentDataType.STRING)
        ) {
            return false;
        }

        var bossKey = entity.getPersistentDataContainer()
                .get(Bosses.BOSS_TAG, PersistentDataType.STRING);

        return boss.getKey().equals(bossKey);
    }

    @Override
    public String getName() {
        return "boss/" + boss.getKey();
    }
}