package net.forthecrown.inventory.weapon.goals;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Getter
public class HouseReforgeGoal implements WeaponGoal {
    @Override
    public int getGoal() {
        return 1;
    }

    @Override
    public boolean test(EntityDamageByEntityEvent event) {
        return false;
    }

    @Override
    public Component loreDisplay() {
        return Component.text("In progress, cannot upgrade further");
    }

    @Override
    public String getName() {
        return "house_reforge";
    }
}