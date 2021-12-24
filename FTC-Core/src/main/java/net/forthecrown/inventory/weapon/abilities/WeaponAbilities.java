package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;

public final class WeaponAbilities {
    private WeaponAbilities() {}

    public static final WeaponAbility
            EXPLODE = register(new ExplodeAbility());

    public static void init() {
        Crown.logger().info("WeaponAbilities initialized");
    }

    private static WeaponAbility register(WeaponAbility ability) {
        return Registries.WEAPON_ABILITIES.register(ability.key(), ability);
    }
}