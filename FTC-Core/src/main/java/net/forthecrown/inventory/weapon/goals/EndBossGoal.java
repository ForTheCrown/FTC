package net.forthecrown.inventory.weapon.goals;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EndBossGoal implements WeaponKillGoal {
    private final int goal, rank;
    private final Key key;
    private final EntityType type;

    public EndBossGoal(EntityType type, int goal, int rank) {
        this.goal = goal;
        this.rank = rank;
        this.type = type;

        this.key = WeaponGoal.createKey(rank, "boss_" + type.name().toLowerCase());
    }

    @Override
    public int getGoal() {
        return goal;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public boolean test(Entity killed) {
        return killed.getType() == type;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public Component loreDisplay() {
        return Component.translatable(Bukkit.getUnsafe().getTranslationKey(type));
    }
}
