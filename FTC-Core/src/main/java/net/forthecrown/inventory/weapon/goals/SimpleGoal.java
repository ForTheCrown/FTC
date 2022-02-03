package net.forthecrown.inventory.weapon.goals;

import net.forthecrown.user.CrownUser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleGoal implements WeaponKillGoal {
    private final int goal, rank;
    private final EntityType type;
    private final Key key;

    public SimpleGoal(int goal, int rank, @Nullable EntityType type) {
        this.goal = goal;
        this.type = type;
        this.rank = rank;
        this.key = WeaponGoal.createKey(rank, type == null ? "any" : type.name().toLowerCase());
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
    public boolean test(CrownUser user, Entity killed) {
        return type == null || killed.getType() == type;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public Component loreDisplay() {
        return type == null ? Component.text("Any entity") : Component.translatable(Bukkit.getUnsafe().getTranslationKey(type));
    }
}
