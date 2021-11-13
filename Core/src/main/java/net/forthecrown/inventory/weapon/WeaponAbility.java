package net.forthecrown.inventory.weapon;

import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;

public interface WeaponAbility extends Keyed {
    void onWeaponUse(WeaponUseContext context);
    void onAltAttack(WeaponUseContext context);

    Component loreDisplay();
}
