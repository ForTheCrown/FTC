package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;

public final class WeaponAbilities {
    private WeaponAbilities() {}

    public static void init() {
        register(new PoofWeaponAbility());

        Crown.logger().info("WeaponAbilities initialized");
    }

    private static void register(WeaponAbility ability) {
        Registries.WEAPON_ABILITIES.register(ability.key(), ability);
    }
}