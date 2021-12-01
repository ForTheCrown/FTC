package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.inventory.weapon.AltAttackContext;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface WeaponAbility extends Keyed {
    void onWeaponUse(WeaponUseContext context);

    default void onAltAttack(AltAttackContext context) {
    }

    void onBlockAltAttack(AltAttackContext.Block context);
    void onEntityAltAttack(AltAttackContext.Entity context);

    Component loreDisplay();

    @Override
    @NotNull Key key();
}
