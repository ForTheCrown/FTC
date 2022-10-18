package net.forthecrown.inventory.weapon.goals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Getter
@RequiredArgsConstructor
public class DamageGoal implements WeaponGoal {
    private final int goal;

    @Override
    public boolean test(EntityDamageByEntityEvent event) {
        return true;
    }

    @Override
    public int getIncrementAmount(EntityDamageByEntityEvent event) {
        return (int) event.getFinalDamage();
    }

    @Override
    public Component loreDisplay() {
        return Component.text("Dealt damage");
    }

    @Override
    public String getName() {
        return "dealt_damage";
    }
}