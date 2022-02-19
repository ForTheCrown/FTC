package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;

public final class WeaponAbilities {
    private WeaponAbilities() {}

    public static void init() {
        register(TestWeaponAbility.TYPE);

        Registries.WEAPON_ABILITIES.close();

        Crown.logger().info("WeaponAbilities initialized");
    }

    private static void register(WeaponAbility.Type type) {
        Registries.WEAPON_ABILITIES.register(type.key(), type);
    }
}