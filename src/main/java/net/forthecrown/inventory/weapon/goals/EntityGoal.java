package net.forthecrown.inventory.weapon.goals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class EntityGoal implements WeaponKillGoal {
    private final int goal;
    private final EntityType type;

    @Override
    public boolean test(User user, Entity killed) {
        return type == null || killed.getType() == type;
    }

    @Override
    public Component loreDisplay() {
        return type == null ? Component.text("Any entity") : Component.translatable(Bukkit.getUnsafe().getTranslationKey(type));
    }

    @Override
    public @NotNull String getName() {
        return "entity/" + (type == null ? "any" : type.name().toLowerCase());
    }
}